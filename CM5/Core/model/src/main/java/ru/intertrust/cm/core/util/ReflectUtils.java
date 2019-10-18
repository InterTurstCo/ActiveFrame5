package ru.intertrust.cm.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Класс содержит статические методы, предоставляющие сервисы, основанные на механизмах java-reflection.
 * @author Gleb Nozdrachev
 */
public abstract class ReflectUtils {

    /**
     * Вычисляет тип (1-й), которым параметризовано возвращаемое значения указанного метода.
     * @param method - метод;
     */
    @Nonnull
    public static Class<?> getGenericReturnTypeParam (final @Nonnull Method method) {

        final Type returnType = method.getGenericReturnType();

        if (!(returnType instanceof ParameterizedType)) {
            throw new RuntimeException("Return type is not ParameterizedType at '" + method + "'");
        }

        final Type genericType = ((ParameterizedType)returnType).getActualTypeArguments()[0];

        if (!(genericType instanceof Class)) {
            throw new RuntimeException("Generic parameter is invalid at '" + method + "'");
        }

        return (Class<?>)genericType;

    }

    /**
     * Вычисляет типы (классы, интерфейсы), которыми параметризован тип <code>parentGenericType</code> в типе <code>childType</code>.
     * @param parentGenericType - родительский generic-тип;
     * @param childType - дочерний тип;
     * <p>
     * @return
     * Возвращаемый массив имеет такое же количество элементов, как и количество generic-параметров в <code>parentGenericType</code>.
     * В массиве <i>i</i>-ый элемент представляет собой тип, которым <code>childType</code> параметризовал <i>i</i>-ый параметр
     * в <code>parentGenericType</code>. Если "в точке" <code>childType</code> какой-то из параметров еще не известен, то
     * соответствующий элемент массива будет <code>== null</code>.
     * <p>
     * В случае если <code>parentGenericType</code> не имеет ни одного generic-параметра, возвращается пустой массив.
     * <p>
     * @throws
     * IllegalArgumentException если (любое):
     * <ul>
     * <li><code>parentGenericType == null</code>;
     * <li><code>childType == null</code>;
     * <li><code>parentGenericType</code> не является родительским для <code>childType</code>;
     * </ul>
     */
    @Nonnull
    public static Class<?>[] computeActualGenericParams (final @Nonnull Class<?> parentGenericType, final @Nonnull Class<?> childType) {

        if (parentGenericType == null) {
            throw new IllegalArgumentException("parentGenericType must be not-null");
        }

        final Class<?>[] result = new Class<?>[parentGenericType.getTypeParameters().length];

        if (result.length == 0) {
            return result;
        }

        final List<Class<?>> path = computeInheritanceChains(parentGenericType, childType).get(0);
        final Integer[] idxs = new Integer[result.length];
        final ListIterator<Class<?>> pathIter = path.listIterator(path.size());
        int cnt = 0;
        Class<?> currClass = pathIter.previous();

        for (int i = 0; i < idxs.length; i++) {
            idxs[i] = i;
        }

        while (pathIter.hasPrevious()) {

            final Class<?> prevClass = currClass;
            currClass = pathIter.previous();
            final Set<Type> parTypes = new HashSet<>(Arrays.asList(currClass.getGenericInterfaces()));
            parTypes.add(currClass.getGenericSuperclass());
            ParameterizedType parGenType = null;

            for (final Type parType : parTypes) {
                if (parType instanceof ParameterizedType) {
                    final ParameterizedType pt = (ParameterizedType)parType;
                    if (pt.getRawType() == prevClass) {
                        parGenType = pt;
                        break;
                    }
                }
            }

            if (parGenType == null) {
                break;
            }

            final Type[] argTypes = parGenType.getActualTypeArguments();
            Map<String, Integer> newIdxs = null;

            for (int i = 0; i < idxs.length; i++) {
                if (idxs[i] != null) {

                    final Type argType = argTypes[idxs[i]];

                    if ((argType instanceof Class) || (argType instanceof ParameterizedType)) {

                        final Type type = (argType instanceof Class) ? argType : ((ParameterizedType)argType).getRawType();
                        result[i] = (Class<?>)type;
                        idxs[i] = null;

                        if (++cnt == result.length) {
                            return result;
                        }

                    } else if (argType instanceof TypeVariable) {

                        if (newIdxs == null) {

                            newIdxs = new HashMap<>();
                            final TypeVariable<?>[] typeVars = currClass.getTypeParameters();

                            for (int q = 0; q < typeVars.length; q++) {
                                newIdxs.put(typeVars[q].getName(), q);
                            }

                        }

                        final String varName = ((TypeVariable<?>)argType).getName();
                        final Integer newIdx = newIdxs.get(varName);

                        if (newIdx == null) {
                            throw new RuntimeException("Unable to resolve variable '" + varName + "' at '" + currClass + "'");
                        }

                        idxs[i] = newIdx;

                    } else {

                        throw new RuntimeException("Unexpected type '" + argType + "' at '" + currClass + "'");

                    }

                }
            }

        }

        return result;

    }

    /**
     * Вычисляет все возможные "цепочки" наследования между двумя указанными типами (классами, интерфейсами).
     * @param parentType - родительский тип;
     * @param childType - дочерний тип;
     * <p>
     * @return
     * <ul>
     * <li>"Внешний" список содержит все возможные цепочки. Этот список никогда не бывает пустым. Список отсортирован по длине цепочки в порядке возрастания.
     * <li>Каждый "внутренний" список представляет собой одну из цепочек. Элементы: <code>[0] childType, ..., [n] parentType</code>. Если
     * <code>parentType == childType</code>, то список состоит из одного элемента.
     * </ul>
     * @throws
     * IllegalArgumentException если (любое):
     * <ul>
     * <li><code>parentType == null</code>;
     * <li><code>childType == null</code>;
     * <li><code>parentType</code> не является родительским для <code>childType</code>;
     * </ul>
     */
    @Nonnull
    public static List<List<Class<?>>> computeInheritanceChains (final @Nonnull Class<?> parentType, final @Nonnull Class<?> childType) {

        if (!(parentType.isAssignableFrom(childType))) {
            throw new IllegalArgumentException("parentType '" + parentType + "' must be assignable from childType '" + childType + "'");
        }

        final List<List<Class<?>>> result = new ArrayList<>();
        processChain(parentType, childType, result, new ArrayList<Class<?>>(1));

        Collections.sort(result, new Comparator<Collection<?>>() {

            @Override
            public int compare (final Collection<?> c1, final Collection<?> c2) {
                return c1.size() - c2.size();
            }

        });

        return result;

    }

    private static void processChain (final Class<?> parentType, final Class<?> currentType, final List<List<Class<?>>> chains, final List<Class<?>> chain) {

        chain.add(currentType);

        if (parentType == currentType) {

            chains.add(chain);

        } else {

            boolean b = false;

            if (!currentType.isInterface()) {
                processSuperType(parentType, currentType.getSuperclass(), chains, chain);
                b = true;
            }

            if (parentType.isInterface() || parentType == Object.class) {
                for (final Class<?> superInterface : currentType.getInterfaces()) {
                    processSuperType(parentType, superInterface, chains, chain);
                    b = true;
                }
            }

            if (!b) {
                processSuperType(parentType, Object.class, chains, chain);
            }

        }

    }

    private static void processSuperType (final Class<?> parentType, final Class<?> superType, final List<List<Class<?>>> chains, final List<Class<?>> chain) {

        if (parentType.isAssignableFrom(superType)) {
            final List<Class<?>> newChain = new ArrayList<>(chain.size() + 1);
            newChain.addAll(chain);
            processChain(parentType, superType, chains, newChain);
        }

    }

    private ReflectUtils () {
    }

}