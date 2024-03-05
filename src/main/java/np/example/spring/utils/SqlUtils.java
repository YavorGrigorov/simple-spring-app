package np.example.spring.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.SequenceGenerator;


public class SqlUtils {
	
	public static String createNativeBatchInsertQuery(Class<?> entity, int batchSize) {
		StringBuilder sql = new StringBuilder("INSERT ALL ");
		
		Field fields[] = entity.getDeclaredFields();
		StringBuilder rowPatternBuilder = new StringBuilder();
		StringBuilder valuesPatternBuilder = new StringBuilder();
		rowPatternBuilder.append("INTO ").append(entity.getSimpleName()).append(" (");
		valuesPatternBuilder.append("(");
		
		for(Field f : fields) {
			Column column = f.getAnnotation(Column.class);
			if(column != null) {
				rowPatternBuilder.append(column.name()).append(", ");
				valuesPatternBuilder.append(":").append(f.getName()).append("%1$d").append(", ");
				continue;
			}
			
			JoinColumn joinColumn = f.getAnnotation(JoinColumn.class);
			if(joinColumn != null) {
				rowPatternBuilder.append(joinColumn.name()).append(", ");
				valuesPatternBuilder.append(":").append(joinColumn.name()).append("%1$d").append(", ");
			}
		}
		rowPatternBuilder.deleteCharAt(rowPatternBuilder.length() - 2);
		rowPatternBuilder.append(") VALUES ");
		valuesPatternBuilder.deleteCharAt(valuesPatternBuilder.length() - 2);
		rowPatternBuilder.append(valuesPatternBuilder).append(") ");
		
		
		for(int i = 0; i < batchSize; ++i) {
			sql.append(String.format(rowPatternBuilder.toString(), i));
		}
		return sql.append(" SELECT * FROM DUAL; ").toString();
	}
	
	public static <T> Map<String, Object> createBatchParameterMap(Class<T> entityClass, List<T> listEntities, int listPos, int batchSize) throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> map = new HashMap<>();

		Field fields[] = entityClass.getDeclaredFields();
		for(Field f : fields) {
			Column column = f.getAnnotation(Column.class);
			if(column != null) {
				Field joinColumnTargetField = null;
				for(int i = 0; i < batchSize && listPos + i < listEntities.size(); ++i) {
					f.setAccessible(true);
					T currEntity = listEntities.get(listPos + i);

					JoinColumn joinColumn = f.getAnnotation(JoinColumn.class);
					ManyToOne mtoAnnotation = f.getAnnotation(ManyToOne.class);
					OneToOne otoAnnotation = f.getAnnotation(OneToOne.class);
					
					if(joinColumn != null && mtoAnnotation != null) {
						Object mtoTarget = f.get(currEntity);
						if(joinColumnTargetField == null) {
							String refColName = joinColumn.referencedColumnName();
							Field mtoTargetFields[] = mtoTarget.getClass().getDeclaredFields();
							for(Field mtoF: mtoTargetFields) {
								Column mtoTC = mtoF.getAnnotation(Column.class);
								if(ObjectUtils.equals(mtoTC.name(), refColName)) {
									joinColumnTargetField = mtoF;
									break;
								}
							}
						}
						
						map.put(joinColumn.name() + i, joinColumnTargetField.get(mtoTarget));
					} else {
						Object value = f.get(currEntity);
						map.put(f.getName() + i, value);
					}
				}
			}
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<Long> getBatchEntityIds(EntityManager em, List<T> listEntities) {
		if(ObjectUtils.isEmpty(listEntities)) return null;
		// Ids might not be completely sequential ;
		List<Long> listIds = new ArrayList<>();
		
		Class<T> entityClass = (Class<T>) listEntities.get(0).getClass();
		Field fields[] = entityClass.getDeclaredFields();
		Field idField = null;
		int allocationSize = 0;
		for(Field f: fields) {
			if(f.getAnnotation(Id.class) != null) {
				idField = f;
				idField.setAccessible(true);
				SequenceGenerator generator = f.getAnnotation(SequenceGenerator.class);
				String sequenceName = generator.sequenceName();
				allocationSize = generator.allocationSize();
				int iterations = listEntities.size() / allocationSize;
				
				String seqNextValUnionStr = String.format("UNION ALL SELECT %s.NEXTVAL FROM DUAL ", sequenceName);
				StringBuilder sql = new StringBuilder(String.format("SELECT %s.NEXTVAL FROM DUAL ", sequenceName));
				for (int i = 0; i < iterations; ++i)
					sql.append(seqNextValUnionStr);
				
				if(listEntities.size() % allocationSize != 0) {
					sql.append(seqNextValUnionStr);
				}
				sql.append(";");
				listIds = (List<Long>) em.createNativeQuery(sql.toString()).getResultList().stream().map(res -> (Long) res).collect(Collectors.toList());
				
				break;
			}
		}
		if(idField == null) return null;
		
		int idx = 0;
		for(int i = 0; i < listEntities.size(); ++i) {
			long currId = listIds.get(idx);
			T e = listEntities.get(i);
			int idInc = (i % allocationSize);
			try {
				idField.set(e, currId + idInc);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			
			if(i != 0 && (idInc == allocationSize - 1)) {
				++idx;
			}
		}
		return listIds;
	}

	
	public static <T> void batchSave(EntityManager em, List<T> listEntities, int batchSize) throws IllegalArgumentException, IllegalAccessException {
		batchSave(em, listEntities, batchSize, (x,y) -> {});
	}

	public static <T> void batchSave(EntityManager em, List<T> listEntities, int batchSize, java.util.function.BiConsumer<T, Long> idPopulator) throws IllegalArgumentException, IllegalAccessException {
		if(ObjectUtils.isEmpty(listEntities)) return;
		
		int iterations = listEntities.size() / batchSize ;

		List<Long> listIds = getBatchEntityIds(em, listEntities);
		@SuppressWarnings("unchecked")
		Class<T> entityClass = (Class<T>) listEntities.get(0).getClass();
		String batchQuery = createNativeBatchInsertQuery(entityClass, batchSize);
		for (int i = 0; i < iterations; ++i) {
			Map<String, Object> paramters = createBatchParameterMap(entityClass, listEntities, i * batchSize, batchSize);
			Query query = em.createNativeQuery(batchQuery);
			paramters.forEach((k,v) -> query.setParameter(k, v));
			em.flush();
		}
		
		int remainder = listEntities.size() % batchSize;
		if ( remainder != 0 ) {
			batchQuery = createNativeBatchInsertQuery(entityClass, remainder);
			Map<String, Object> paramters = createBatchParameterMap(entityClass, listEntities, listEntities.size() - remainder, remainder);
			Query query = em.createNativeQuery(batchQuery);
			paramters.forEach((k,v) -> query.setParameter(k, v));
			em.flush();
		}
	}
	
}