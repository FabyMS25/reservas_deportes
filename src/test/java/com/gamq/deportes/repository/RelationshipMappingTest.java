package com.gamq.deportes.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.jpa.repository.Query;
import javax.persistence.Entity;

/** Build actual Hibernate mappings and parse repository queries without touching a database. */
class RelationshipMappingTest {
    @Test void mappingsAndRelationshipQueriesAreValid() throws Exception {
        StandardServiceRegistry registry=new StandardServiceRegistryBuilder()
            .applySetting("hibernate.dialect","org.hibernate.dialect.PostgreSQL10Dialect")
            .applySetting("hibernate.temp.use_jdbc_metadata_defaults","false")
            .applySetting("hibernate.hbm2ddl.auto","none").build();
        try {
            MetadataSources sources=new MetadataSources(registry);
            ClassPathScanningCandidateComponentProvider scanner=new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
            for (org.springframework.beans.factory.config.BeanDefinition bean:scanner.findCandidateComponents("com.gamq.deportes.model"))
                sources.addAnnotatedClass(Class.forName(bean.getBeanClassName()));
            try (SessionFactory factory=sources.buildMetadata().buildSessionFactory(); Session session=factory.openSession()) {
                for (Class<?> repository:new Class<?>[]{OrganizacionRepository.class,EstudianteEscuelaRepository.class})
                    for (java.lang.reflect.Method method:repository.getDeclaredMethods()) {
                        Query query=method.getAnnotation(Query.class);
                        if (query!=null && !query.nativeQuery()) session.createQuery(query.value().replaceAll("%(:[A-Za-z][A-Za-z0-9]*)%", "$1"));
                    }
            }
        } finally { StandardServiceRegistryBuilder.destroy(registry); }
    }
}
