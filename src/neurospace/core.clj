(ns neurospace.core)

(defrecord Clazz [name access])

(defrecord Method [owner name access desc signature caller])

(defrecord Field [owner name access desc caller])

(defrecord MappedClass [name ob-name])
(defrecord MappedMethod [name owner signature
                         ob-name ob-owner ob-signature])
(defrecord MappedField [name owner ob-name ob-owner])
