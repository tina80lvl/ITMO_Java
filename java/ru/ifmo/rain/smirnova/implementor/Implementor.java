package ru.ifmo.rain.smirnova.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Implementation of {@link JarImpler}.
 * This class provides methods for generating source code of given type token
 * and creating jar file containing it.
 *
 * @author Valentina Smirnova
 */
public class Implementor implements JarImpler {

    /**
     * Full path of source file to generate.
     */
    private Path srcFile;

    /**
     * Constructs default implementor instance.
     */
    public Implementor() {
        srcFile = null;
    }

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (jarFile.getParent() != null) {
            try {
                Files.createDirectories(jarFile.getParent());
            } catch (IOException e) {
                throw new ImplerException("Couldn't create jar file", e);
            }
        }
        Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "tmp");
        } catch (IOException e) {
            throw new ImplerException("Couldn't create temp directory", e);
        }
        implement(token, tmpDir);
        compile(srcFile, tmpDir);
        try (JarOutputStream jarOut = new JarOutputStream(Files.newOutputStream(jarFile), createManifest())) {
            try {
                jarOut.putNextEntry(new ZipEntry(token.getName().replace('.', '/') + "Impl.class"));
                Files.copy(tmpDir.resolve(token.getCanonicalName().replace(".", File.separator) + "Impl.class"), jarOut);
            } catch (IOException e) {
                throw new ImplerException("Couldn't write to jar file", e);
            }
        } catch (IOException e) {
            throw new ImplerException("Couldn't create jar file", e);
        } catch (InvalidPathException e) {
            throw new ImplerException("Given jar path is incorrect", e);
        }
        try {
            clean(tmpDir);
        } catch (IOException e) {
            throw new ImplerException("Couldn't delete temp directory", e);
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token.isPrimitive() || token.isArray() || Modifier.isFinal(token.getModifiers()) || token.equals(Enum.class)) {
            throw new ImplerException("The given token is incorrect.");
        }
        try {
            createFile(token, root);
        } catch (IOException e) {
            throw new ImplerException("Couldn't create a source file", e);
        }
        try (UnicodeWriter writer = new UnicodeWriter(Files.newOutputStream(srcFile))) {
            Set<Integer> methods = new TreeSet<>();
            CodeGenerator generator = new CodeGenerator(token.getSimpleName() + "Impl", System.lineSeparator(), "    ");
            writer.write(generator.generateClassDeclaration(token));
            for (Method m : token.getMethods()) {
                if (Modifier.isAbstract(m.getModifiers()) && methods.add(getHash(m))) {
                    writer.write(generator.generateMethod(m));
                }
            }
            writer.write(generator.generateEndOfClassDeclaration());
        } catch (IOException e) {
            throw new ImplerException("Couldn't write to the source file", e);
        }
    }

    /**
     * Creates manifest for generated jar file.
     *
     * @return created Manifest with set Manifest-Version and Created-By attributes.
     */
    private Manifest createManifest() {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Valentina Smirnova");
        return manifest;
    }

    /**
     * Compiles given source file and place class file in the same directory.
     *
     * @param src  - full name of source file.
     * @param root - root full name.
     * @throws ImplerException If error during getting java compiler occurred
     */
    private void compile(Path src, Path root) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Couldn't get java compiler");
        }
        compiler.run(null, null, null,
                src.toAbsolutePath().toString(),
                "-cp",
                root.toString() + File.pathSeparator + System.getProperty("java.class.path")
        );
    }

    /**
     * Creates source file to contain generated code. The path of the file matches to its package.
     * Root package places in given root directory.
     *
     * @param token type token to generate source file for
     * @param root  path of root directory to contain root package of given token
     * @throws IOException If error during creating directories occurred
     */
    private void createFile(Class<?> token, Path root) throws IOException {
        srcFile = root.resolve(token.getCanonicalName().replace(".", File.separator) + "Impl.java");
        if (srcFile.getParent() != null) {
            Files.createDirectories(srcFile.getParent());
        }
        Files.createFile(srcFile);
    }

    /**
     * Calculates hash of given method according to its return type and parameters.
     *
     * @param m - method to calculate hash of
     * @return value of calculated hash
     */
    private int getHash(Method m) {
        return (m.getName() + Arrays.toString(m.getParameterTypes())).hashCode();
    }

    /**
     * Provides possibility to run Implementor by using a command line.
     * In case of any error console description message is printed.
     * <p>
     * Usage:
     * <ul>
     * <li> <tt>Implementor full-class-name root-path</tt> - invokes {@link #implement(Class, Path)} with given arguments </li>
     * <li> <tt>Implementor -jar full-class-name jar-path</tt> - invokes {@link #implementJar(Class, Path)} with given second and third arguments </li>
     * </ul>
     *
     * @param args - the command line parameters
     */
    public static void main(String[] args) {
        if (args == null || args.length != 2 && args.length != 3 || args[0] == null || args[1] == null || args.length == 3 && (args[2] == null || !args[0].equals("-jar"))) {
            System.out.println("Incorrect arguments.\nUsage:\nImplementor <full-class-name> <root-path>\nor\nImplementor -jar <full-class-name> <jar-path>\n");
            return;
        }
        JarImpler implementor = new Implementor();
        try {
            switch (args.length) {
                case 2:
                    implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
                    break;
                case 3:
                    implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
                    break;
            }
        } catch (ImplerException e) {
            System.out.println("Couldn't implement given type:\n" + e.getMessage());
        } catch (InvalidPathException e) {
            System.out.println("Given path is incorrect:\n" + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find given type:\n" + e.getMessage());
        }
    }

    /**
     * Recursively deletes directory or file according to given {@link Path}
     *
     * @param root path of directory or file to delete
     * @throws IOException if an error during deleting occurred
     */
    private void clean(final Path root) throws IOException {
        SimpleFileVisitor<Path> cleaner = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        };
        if (!Files.exists(root)) {
            return;
        }
        Files.walkFileTree(root, cleaner);
    }
}

//package ru.ifmo.rain.smirnova.implementor;
//
//import info.kgeorgiy.java.advanced.implementor.Impler;
//import info.kgeorgiy.java.advanced.implementor.ImplerException;
//
//import java.io.*;
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.stream.Collectors;
//
//public class Implementor implements Impler {
//    private Path srcFile;
//    private String className;
//    private static final String newLine = System.lineSeparator();
//    private static final String tab = "    ";
//
//    private void createFile(Class<?> token, Path root) throws IOException {
//        className = token.getSimpleName() + "Impl";
//        srcFile = Paths.get(
//                root
//                        + File.separator
//                        + token.getCanonicalName()
//                        .replace(".", File.separator)
//                        + "Impl.java");
//        Files.createDirectories(srcFile.getParent());
//        Files.createFile(srcFile);
//    }
//
//    private void implementInterface(Class<?> clazz, Writer writer) throws IOException {
//        for (Method m : clazz.getMethods()) {
//                writer.write(generateMethod(m));
//        }
//    }
//
//    private String generateMethod(Method m) {
//        Class<?>[] exceptions = m.getExceptionTypes();
//        return  getModifiers(m) + " "
//                + m.getReturnType().getCanonicalName() + " "
//                + m.getName()
//                + "("
//                + String.join(", ",
//                Arrays.stream(m.getParameters())
//                        .map(p -> p.getType().getCanonicalName() + " " + p.getName())
//                        .collect(Collectors.toList()))
//                + ") "
//                + (exceptions.length != 0 ?
//                "throws "
//                        + String.join(", ",
//                        Arrays.stream(exceptions)
//                                .map(Class::getCanonicalName)
//                                .collect(Collectors.toList())) : "")
//                + " {" + newLine
//                + tab + "return " + getDefaultValue(m.getReturnType()) + ";" + newLine
//                + "}" + newLine + newLine;
//    }
//
//    private String getModifiers(Method m) {
//        int x = m.getModifiers();
//        return (Modifier.isPrivate(x) ? "private " : "") + (Modifier.isProtected(x) ? "protected " : "") + (Modifier.isPublic(x) ? "public " : "")
//                + (Modifier.isStatic(x) ? "static " : "") + (Modifier.isFinal(x) ? "final " : "");
//    }
//
//    private String getDefaultValue(Class<?> clazz) {
//        String typeName = clazz.getCanonicalName();
//        if (typeName.equals("byte"))
//            return "0";
//        if (typeName.equals("short"))
//            return "0";
//        if (typeName.equals("int"))
//            return "0";
//        if (typeName.equals("long"))
//            return "0L";
//        if (typeName.equals("char"))
//            return "'\u0000'";
//        if (typeName.equals("float"))
//            return "0.0F";
//        if (typeName.equals("double"))
//            return "0.0";
//        if (typeName.equals("boolean"))
//            return "false";
//        if (typeName.equals("void"))
//            return "";
//        return "null";
//    }
//
//    @Override
//    public void implement(Class<?> token, Path root) throws ImplerException {
//        if (token.isArray() || token.isPrimitive()) {
//            throw new ImplerException("The given token is incorrect: array or primitive type is provided.");
//        }
//        try {
//            createFile(token, root);
//        } catch (IOException e) {
//            throw new ImplerException("Couldn't create a source file", e);
//        }
//        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(srcFile), "UTF-8")) {
//            writer.write( "package " + token.getPackage().getName() + ";" + newLine + newLine
//                    + "public class " + className + " " + (token.isInterface() ? "implements" : "extends") + " " + token.getCanonicalName() + " {" + newLine);
//            //may be extends & implements???
//            implementInterface(token, writer);
//            writer.write("}");
//        } catch (IOException e) {
//            throw new ImplerException("Couldn't write to the source file", e);
//        }
//    }
//}