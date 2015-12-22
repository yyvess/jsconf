package net.jmob.jsconf.core.service;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.util.LinkedHashSet;
import java.util.Set;

public class ClassPathScanningCandidate extends ClassPathScanningCandidateComponentProvider {

    public ClassPathScanningCandidate(boolean useDefaultFilters) {
        super(useDefaultFilters);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return true;
    }

    public Set<Class<?>> findCandidateClass(String forPackage) {
        Set<Class<?>> candidates = new LinkedHashSet<>();
        Set<BeanDefinition> candidateComponents = super.findCandidateComponents(forPackage);
        for (BeanDefinition cl : candidateComponents) {
            try {
                candidates.add(Thread.currentThread().getContextClassLoader().loadClass(cl.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                // Not a candidates
            }
        }
        return candidates;
    }
}
