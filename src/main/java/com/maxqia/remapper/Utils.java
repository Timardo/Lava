package com.maxqia.remapper;

import org.objectweb.asm.Type;

import java.util.Map;

public class Utils {
    public static String reverseMapExternal(Class<?> name) {
        return reverseMap(name).replace('$', '.').replace('/', '.');
    }

    public static String reverseMap(Class<?> name) {
        return reverseMap(Type.getInternalName(name));
    }

    public static String reverseMap(String check) {
        return Transformer.jarMapping.classes.getOrDefault(check, check);
    }

    public static String mapMethod(Class<?> inst, String name, Class<?>... parameterTypes) {
        String result = mapMethodInternal(inst, name, parameterTypes);
        if (result != null) {
            return result;
        }
        return name;
    }

    public static String mapMethodInternal(Class<?> inst, String name, Class<?>... parameterTypes) {
        String match = reverseMap(inst) + "/" + name;

        Map<String, String> map = Transformer.jarMapping.methods;
        for (String value : map.keySet()) {
        	boolean failed = false;
        	if (value.startsWith(match)) {
                String params = value.split("\\s+")[1];
                Type[] types = Type.getArgumentTypes(params);
                
                if (types.length != parameterTypes.length) { //skip entry if number of params doesn't match - a bit more effective
                	continue;
                }
                
                for (int i = 0; i < types.length; i++) {
                    if (!types[i].getClassName().replace('$', '.').replace('/', '.').equals(reverseMapExternal(parameterTypes[i]))) { //add replace to avoid problems with subclasses and $ char
                    	failed = true;
                        break;
                    }
                }
            }

        	if (!failed) {
                return map.get(value);
            }
        }

        Class<?> interfaces = inst.getSuperclass();
        if (interfaces != null) {
            String superMethodName = mapMethodInternal(interfaces, name, parameterTypes);
            return String.valueOf(superMethodName);
        }
        return null;
    }
}
