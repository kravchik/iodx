package yk.lang.iodx;

import yk.lang.iodx.utils.Reflector;
import yk.ycollections.Tuple;
import yk.ycollections.YList;
import yk.ycollections.YMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static yk.ycollections.Tuple.tuple;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Serializes Java objects to IodxEntity representation.
 * 
 * Converts Java objects to IodxEntity objects that can be printed by IodxPrinter
 * and deserialized back by IodxJavaFromEntity.
 * 
 * Current support:
 * - String: passes through unchanged
 * - Primitives (Integer, Long, Float, Double, Boolean, Character): pass through unchanged
 * - List: converts to YList (without wrapper)
 * - Map: converts to IodxEntity without name, all elements as Tuple
 * - Objects: converts to IodxEntity with class simple name and field tuples (only for explicitly allowed classes)
 */
public class IodxJavaToEntity {
    
    private final YMap<Class<?>, String> availableClasses;
    private IdentityHashMap<Object, Tuple<IodxEntity, Integer>> identity = new IdentityHashMap<>();
    private int nextRefId = 1;
    private boolean skipDefaultValues = true;
    private boolean allClassesAvailable = true;
    private boolean allowInstantiationWithoutDefaultConstructor = true;

    private YMap<Class, Function> serializerByClass = hm();
    
    /**
     * Constructor that specifies which classes are allowed for object serialization.
     *
     * @param classes classes that can be serialized as objects
     */
    public IodxJavaToEntity(Class<?>... classes) {
        this.availableClasses = al(classes).toMap(v -> v, v -> v.getSimpleName());
    }

    public IodxJavaToEntity addImport(Class<?> clazz) {
        availableClasses.put(clazz, clazz.getSimpleName());
        return this;
    }

    public <T> IodxJavaToEntity addSerializerByClass(Class<T> c, Function<T, Object> converter) {
        serializerByClass.put(c, converter);
        return this;
    }
    
    /**
     * Main entry point for serialization.
     * 
     * @param obj Java object to serialize
     * @return IodxEntity representation or the object unchanged if it's a primitive
     */
    public Object serialize(Object obj) {
        // Clear identity map for each top-level serialization
        identity.clear();
        nextRefId = 1;
        
        Object result = serializeImpl(obj);
        
        // Resolve references after serialization
        resolveRefs();
        
        return result;
    }
    
    /**
     * Internal serialization with reference tracking.
     * 
     * @param obj Java object to serialize
     * @return IodxEntity representation or the object unchanged if it's a primitive
     */
    private Object serializeImpl(Object obj) {
        if (obj == null) {
            return null;
        }

        if (serializerByClass.containsKey(obj.getClass())) {
            return serializerByClass.get(obj.getClass()).apply(obj);
        }

        if (obj instanceof String) {
            // Strings pass through unchanged
            return obj;
        }
        
        // Primitive types pass through unchanged
        if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || 
            obj instanceof Double || obj instanceof Boolean || obj instanceof Character || obj instanceof Short) {
            return obj;
        }

        // For reference-tracked objects (Lists, Maps, Objects), check if already serialized
        if (obj instanceof List || obj instanceof Map || availableClasses.containsKey(obj.getClass()) || allClassesAvailable) {
            if (identity.containsKey(obj)) {
                Tuple<IodxEntity, Integer> tuple = identity.get(obj);
                if (tuple.b == 0) {
                    // Mark for reference creation
                    tuple.b = nextRefId++;
                }
                // Return reference placeholder
                return new IodxEntity("ref", al(tuple.b));
            }
            
            // Create tuple for tracking
            Tuple<IodxEntity, Integer> tuple = new Tuple<>(null, 0);
            identity.put(obj, tuple);
            
            IodxEntity result;
            if (obj instanceof List) {
                result = new IodxEntity(null, serializeList((List<?>) obj));
            } else if (obj instanceof Map) {
                Object mapResult = serializeMap((Map<?, ?>) obj);
                if (mapResult instanceof IodxEntity) {
                    result = (IodxEntity) mapResult;
                } else {
                    // Empty map case - return directly without wrapper
                    return mapResult;
                }
            } else {
                result = serializeObject(obj);
            }
            
            tuple.a = result;
            return result;
        }
        
        // Throw exception for unsupported types
        throw new RuntimeException("Unsupported object type for serialization: " + obj.getClass().getName() + ", value: " + obj);
    }
    
    /**
     * Serializes a List to YList (without wrapper).
     * 
     * @param list the List to serialize
     * @return YList with serialized children
     */
    private YList<Object> serializeList(List<?> list) {
        YList<Object> serializedChildren = al();
        
        for (Object item : list) {
            // Recursively serialize each child
            serializedChildren.add(serializeImpl(item));
        }
        
        return serializedChildren;
    }
    
    /**
     * Serializes a Map to IodxEntity without name, all elements as Tuple.
     * For empty maps, returns the map directly so IodxPrinter can handle the (=) special case.
     * 
     * @param map the Map to serialize
     * @return IodxEntity with serialized key-value pairs as Tuples, or the Map itself if empty
     */
    private Object serializeMap(Map<?, ?> map) {
        if (map.isEmpty()) {
            // Return the map directly so IodxPrinter can print it as (=)
            return map;
        }
        
        YList<Object> serializedChildren = al();
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            // Each entry becomes a Tuple with serialized key and value
            Object serializedKey = serializeImpl(entry.getKey());
            Object serializedValue = serializeImpl(entry.getValue());
            serializedChildren.add(tuple(serializedKey, serializedValue));
        }
        
        return new IodxEntity(null, serializedChildren);
    }
    
    /**
     * Serializes an object to IodxEntity with class simple name and field tuples.
     * 
     * @param obj the object to serialize
     * @return IodxEntity with class name and field tuples
     */
    private IodxEntity serializeObject(Object obj) {
        YList<Object> children = al();
        
        // Create instance with default values for comparison if skipDefaultValues is enabled
        Object defaults = skipDefaultValues
            ? Reflector.newInstanceArgless(obj.getClass(), allowInstantiationWithoutDefaultConstructor)
            : null;
        
        // Get all fields using reflection
        for (Field field : Reflector.getAllFieldsInHierarchy(obj.getClass())) {
            // Skip static and transient fields
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;
            
            field.setAccessible(true);
            Object value = Reflector.get(obj, field);
            
            // Skip fields with default values if skipDefaultValues is enabled
            if (defaults != null) {
                Object defaultValue = Reflector.get(defaults, field);
                if (value == defaultValue) continue; // Same reference (both null, etc.)
                if (value != null && value.equals(defaultValue)) continue; // Equal values
            }

            Object serializedValue = serializeImpl(value); // recursive serialization
            children.add(tuple(field.getName(), serializedValue));
        }

        String name = availableClasses.get(obj.getClass());
        return new IodxEntity(name == null ? obj.getClass().getSimpleName() : name, children);
    }
    
    /**
     * Resolves references after serialization is complete.
     * Replaces duplicate objects with ref(id, original) constructs.
     */
    private void resolveRefs() {
        for (Tuple<IodxEntity, Integer> tuple : identity.values()) {
            if (tuple.b > 0) {
                // Create a copy of the original entity with same children
                YList<Object> childrenCopy = al();
                childrenCopy.addAll(tuple.a.children);
                IodxEntity copy = new IodxEntity(tuple.a.name, childrenCopy);
                
                // Replace the original with ref(id, copy)
                tuple.a.name = "ref";
                tuple.a.children = al(tuple.b, copy);
            }
        }
    }

    public IodxJavaToEntity setSkipDefaultValues(boolean skipDefaultValues) {
        this.skipDefaultValues = skipDefaultValues;
        return this;
    }

    public IodxJavaToEntity setAllClassesAvailable(boolean allClassesAvailable) {
        this.allClassesAvailable = allClassesAvailable;
        return this;
    }

    public IodxJavaToEntity setAllowInstantiationWithoutDefaultConstructor(boolean allowInstantiationWithoutDefaultConstructor) {
        this.allowInstantiationWithoutDefaultConstructor = allowInstantiationWithoutDefaultConstructor;
        return this;
    }
}
