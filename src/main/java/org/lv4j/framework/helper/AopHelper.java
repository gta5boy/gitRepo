package org.lv4j.framework.helper;

import org.lv4j.framework.annatation.Aspect;
import org.lv4j.framework.annatation.Service;
import org.lv4j.framework.proxy.AspectProxy;
import org.lv4j.framework.proxy.Proxy;
import org.lv4j.framework.proxy.ProxyManager;
import org.lv4j.framework.proxy.TransactionProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 *aop帮助类
 */
public final class AopHelper {
    private static final Logger LOGGER= LoggerFactory.getLogger(AopHelper.class);
    static {
        try {
            Map<Class<?>,Set<Class<?>>> proxyMap=createProxyMap();
            Map<Class<?>,List<Proxy>> targetMap=createTargetMap(proxyMap);
            for (Map.Entry<Class<?>,List<Proxy>> targetEntry:targetMap.entrySet()){
                Class<?> targetClass=targetEntry.getKey();
                List<Proxy> proxyList=targetEntry.getValue();
                Object proxy= ProxyManager.createProxy(targetClass,proxyList);
                BeanHelper.setBean(targetClass,proxy);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addTransactionProxy(Map<Class<?>,Set<Class<?>>> proxyMap){
        Set<Class<?>> serviceClassSet=ClassHelper.getClassByAnnotation(Service.class);
        proxyMap.put(TransactionProxy.class,serviceClassSet);
    }

    private static void addAspectProxy(Map<Class<?>,Set<Class<?>>> proxyMap) throws Exception {
        Set<Class<?>> proxyClassSet=ClassHelper.getClassSetBySuper(AspectProxy.class);
        for (Class<?> proxyClass:proxyClassSet){
            if (proxyClass.isAnnotationPresent(Aspect.class)){
                Aspect aspect=proxyClass.getAnnotation(Aspect.class);
                Set<Class<?>> targetClassSet=createTargetClassSet(aspect);
                proxyMap.put(proxyClass,targetClassSet);
            }
        }

    }

    private static Set<Class<?>> createTargetClassSet(Aspect aspect) throws Exception{
        Set<Class<?>> targetClassSet=new HashSet<Class<?>>();
        Class<? extends Annotation> annotation=aspect.value();
        if (annotation !=null && !annotation.equals(Aspect.class)){
            targetClassSet.addAll(ClassHelper.getClassByAnnotation(annotation));
        }
        return targetClassSet;
    }

    private static Map<Class<?>,Set<Class<?>>>  createProxyMap() throws Exception{
        Map<Class<?>,Set<Class<?>>> proxyMap=new HashMap<Class<?>,Set<Class<?>>>();
        Set<Class<?>> proxySet=ClassHelper.getClassSetBySuper(Aspect.class);
        for (Class<?> proxyClass:proxySet){
            if (proxyClass.isAnnotationPresent(Aspect.class)){
                Aspect aspect=proxyClass.getAnnotation(Aspect.class);
                Set<Class<?>> targeTClassSet=createTargetClassSet(aspect);
                proxyMap.put(proxyClass,targeTClassSet);
            }
        }
        return proxyMap;
    }



    private static Map<Class<?>,List<Proxy>> createTargetMap(Map<Class<?>,Set<Class<?>>> proxyMap) throws Exception{
        Map<Class<?>,List<Proxy>> targetMap=new HashMap<Class<?>,List<Proxy>>();
        for (Map.Entry<Class<?>, Set<Class<?>>> proxyEntry:proxyMap.entrySet()){
            Class<?> proxyClass=proxyEntry.getKey();
            Set<Class<?>> targetClassSet=proxyEntry.getValue();
            for (Class<?> targetClass:targetClassSet){
                Proxy proxy= (Proxy) proxyClass.newInstance();
                if (targetMap.containsKey(targetClass)){
                    targetMap.get(targetClass).add(proxy);
                }else {
                    List<Proxy> proxyList=new ArrayList<Proxy>();
                    proxyList.add(proxy);
                    targetMap.put( targetClass,proxyList);
                }
            }
        }
        return targetMap;
    }
}