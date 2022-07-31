package edu.reversing.asm;

import java.nio.file.Path;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;

import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * Represents the files loaded from a java archive (JAR)
 *
 * @author doga
 * @author Cameron Upson
 */
public class Library implements Iterable<ClassNode> {

    private final Map<String, ClassNode> entries = new HashMap<>();
    private final List<ZipEntry> resources = new ArrayList<>();

    /**
     * Reads a library from a specified path
     *
     * @param source the file path of the archive you wish to load
     * @throws IOException if an I/O error or zip format error has occurred
     */
    public void read(Path source, int flags) throws IOException {
        read(new ZipFile(source.toFile()), flags);
    }

    /**
     * Reads a library from an instance of a {@link ZipFile}
     *
     * @param file the {@link ZipFile} you wish to load
     * @param flags option flags that can be used to modify the default behavior the classes
     * @throws IOException if an I/O error or zip format error has occurred
     */
    public void read(ZipFile file, int flags) throws IOException {
        Enumeration<? extends ZipEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                read(file.getInputStream(entry), flags);
            } else {
                resources.add(entry);
            }
        }
    }

    /**
     * Reads an entry from a provided {@link InputStream}
     *
     * @param stream the {@link InputStream} of the entry being added
     * @param flags option flags that can be used to modify the default behavior of the {@link ClassReader}
     * @throws IOException if an I/O error or zip format error has occurred
     */
    public void read(InputStream stream, int flags) throws IOException {
        ClassNode node = new ClassNode();
        new ClassReader(stream).accept(node, flags);
        add(node);
    }

    /**
     * Reads an entry an array of bytes
     *
     * @param bytes the payload of the entry being added
     * @param flags option flags that can be used to modify the default behavior of the {@link ClassReader}
     */
    public void read(byte[] bytes, int flags) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, flags);
        add(node);
    }

    /**
     * Writes the library to a file
     *
     * TODO: if multiple archives are loaded via add(ZipFile, int) should we save them as multiple archives? or should they be stored in 1 file like the method below already does
     *
     * @param target the location to save the file
     * @param flags option flags that can be used to modify the default behavior of the {@link ClassWriter}
     */
    public void write(Path target, int flags) {
        try (JarOutputStream stream = new JarOutputStream(new FileOutputStream(target.toFile()))) {
            for (Map.Entry<String, ClassNode> entry : entries.entrySet()) {
                stream.putNextEntry(new JarEntry(entry.getValue().name + ".class"));
                ClassWriter writer = new ClassWriter(flags);
                entry.getValue().accept(writer);
                stream.write(writer.toByteArray());
                stream.closeEntry();
            }

            for (ZipEntry entry : resources) {
                stream.putNextEntry(entry);
            }

            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds a {@link ClassNode} with a given name
     *
     * @param name the name of the class
     * @return the class with the provided name
     */
    public ClassNode lookup(String name) {
        return entries.get(name);
    }

    /**
     * Adds a {@link ClassNode} to the entries
     *
     * @param cls the {@link ClassNode} to add
     */
    public void add(ClassNode cls) {
        entries.put(cls.name, cls);
    }

    /**
     * Checks to see if a {@link ClassNode} with a given name is loaded into the entries
     *
     * @param name the name of the class
     * @return the result of whether it is (true) or not (false) in the entries
     */
    public boolean loaded(String name) {
        return entries.containsKey(name);
    }

    /**
     * @return an {@link Iterator} over the elements in this Library
     */
    @Override
    public Iterator<ClassNode> iterator() {
        return entries.values().iterator();
    }
}
