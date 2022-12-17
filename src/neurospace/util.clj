(ns neurospace.util
  (:import [java.lang.reflect Method]
           [java.nio.file Files Paths]
           [java.nio.file.attribute FileAttribute]
           [java.net URI URL URLClassLoader]
           [sun.net.www.protocol.jar JarURLConnection$JarURLInputStream])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn str->int [s]
  (let [^"[C" chars (.toCharArray ^String s)
        len (alength chars)]
    (loop [i 0, sum 0.0]
      (if (< i len)
        (recur (inc i) (+ sum (Character/getNumericValue (aget chars i))))
        sum))))

(defn seek
  "Returns the first entry in coll for which pred returns non-nil."
  [pred coll]
  (reduce #(when (pred %2) (reduced %2)) nil coll))

;; File utils

(defn as-uri
  "Coerce f to URI."
  ^URI [f]
  (-> f io/as-file io/as-url str (URI.)))

(defn ->symlink
  "Create symlink pointing to target."
  [symlink target]
  (let [l (Paths/get (as-uri symlink))
        t (Paths/get (as-uri target))]
    (Files/createSymbolicLink l t (make-array FileAttribute 0))))

(defn symlink->target
  "Returns the target file of symlink."
  [symlink]
  (-> symlink
      io/as-file
      .toPath
      Files/readSymbolicLink
      .toString))

;; Runtime utils

(defn download-jar
  "Downloads and returns artifact-url as a resource."
  [artifact-url]
  (let [artifact-url (io/as-url artifact-url)
        file-name (last (str/split artifact-url #"/"))
        target-path (str (System/getProperty "user.dir")
                         "/resources/"
                         file-name)]
    (when-not (io/resource file-name)
      (with-open [r (io/input-stream artifact-url)
                  w (io/output-stream target-path)]
        (io/copy r w)))
    (io/resource file-name)))

(defn ->url-class-loader
  "Returns a new URLClassLoader for urls."
  (^URLClassLoader [urls]
   (URLClassLoader. (into-array URL (map io/as-url urls))))
  (^URLClassLoader [urls ^ClassLoader parent]
   (URLClassLoader. (into-array URL (map io/as-url urls)) parent)))

(defn find-class
  "Finds and returns class named by class-name using class-loader."
  ^Class [class-loader class-name]
  (doto ^Method (seek #(= (.getName ^Method %) "findClass")
                      (.getDeclaredMethods ^Class
                                           (type class-loader)))
    (.setAccessible true)
    (.invoke ^Method class-loader
             (into-array Object [(str/replace class-name "/" ".")]))))

(defn class->class-file
  "Returns the class file path for clazz."
  [^Class clazz]
  (-> (.getName clazz)
      (str/replace "." "/")
      (str ".class")))

(defn class->input-stream
  "Returns a sun.net.www.protocol.jar.JarURLConnection$JarURLInputStream
  for clazz, if found. Returns nil otherwise."
  ^JarURLConnection$JarURLInputStream [^Class clazz]
  (try
    (-> (.getClassLoader clazz)
        (.getResourceAsStream ^URLClassLoader (class->class-file clazz)))
    (catch NullPointerException _)))
