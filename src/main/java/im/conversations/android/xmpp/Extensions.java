package im.conversations.android.xmpp;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import eu.siacs.conversations.xml.Element;
import im.conversations.android.annotation.XmlElement;
import im.conversations.android.annotation.XmlPackage;
import im.conversations.android.xmpp.model.Extension;
import im.conversations.android.xmpp.model.roster.Item;
import im.conversations.android.xmpp.model.roster.Query;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Extensions {

    private static final List<Class<? extends Extension>> ELEMENTS =
            Arrays.asList(Query.class, Item.class);

    private static final Map<Id, Class<? extends Extension>> EXTENSION_CLASS_MAP;

    static {
        final var builder = new ImmutableMap.Builder<Id, Class<? extends Extension>>();
        for (final Class<? extends Extension> clazz : ELEMENTS) {
            builder.put(id(clazz), clazz);
        }
        EXTENSION_CLASS_MAP = builder.build();
    }

    private static Id id(final Class<? extends Extension> clazz) {
        final XmlElement xmlElement = clazz.getAnnotation(XmlElement.class);
        final Package clazzPackage = clazz.getPackage();
        final XmlPackage xmlPackage =
                clazzPackage == null ? null : clazzPackage.getAnnotation(XmlPackage.class);
        if (xmlElement == null) {
            throw new IllegalStateException(
                    String.format("%s is not annotated as @XmlElement", clazz.getName()));
        }
        final String packageNamespace = xmlPackage == null ? null : xmlPackage.namespace();
        final String elementName = xmlElement.name();
        final String elementNamespace = xmlElement.namespace();
        final String namespace;
        if (!Strings.isNullOrEmpty(elementNamespace)) {
            namespace = elementNamespace;
        } else if (!Strings.isNullOrEmpty(packageNamespace)) {
            namespace = packageNamespace;
        } else {
            throw new IllegalStateException(
                    String.format("%s does not declare a namespace", clazz.getName()));
        }
        final String name;
        if (Strings.isNullOrEmpty(elementName)) {
            name = clazz.getSimpleName().toLowerCase(Locale.ROOT);
        } else {
            name = elementName;
        }
        return new Id(name, namespace);
    }

    public static Element create(final String name, final String namespace) {
        final Class<? extends Element> clazz = of(name, namespace);
        if (clazz == null) {
            return new Element(name, namespace);
        }
        final Constructor<? extends Element> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(
                    String.format("%s has no default constructor", clazz.getName()));
        }
        try {
            return constructor.newInstance();
        } catch (final IllegalAccessException
                | InstantiationException
                | InvocationTargetException e) {
            throw new IllegalStateException(
                    String.format("%s has inaccessible default constructor", clazz.getName()));
        }
    }

    private static Class<? extends Element> of(final String name, final String namespace) {
        return EXTENSION_CLASS_MAP.get(new Id(name, namespace));
    }

    private Extensions() {}

    public static class Id {
        public final String name;
        public final String namespace;

        public Id(String name, String namespace) {
            this.name = name;
            this.namespace = namespace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return Objects.equal(name, id.name) && Objects.equal(namespace, id.namespace);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, namespace);
        }
    }
}
