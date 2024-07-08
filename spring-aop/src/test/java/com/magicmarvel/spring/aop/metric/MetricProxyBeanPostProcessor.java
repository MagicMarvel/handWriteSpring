package com.magicmarvel.spring.aop.metric;

import org.magicmarvel.spring.aop.beanPostProcessor.AnnotationProxyBeanPostProcessor;
import org.magicmarvel.spring.context.annotation.Component;


@Component
public class MetricProxyBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Metric> {

}
