
(ns exercise9)


(defmulti multimethod-type-namer (fn [thing] (type thing)))
(defmethod multimethod-type-namer java.lang.String [thing]
 "String")
(defmethod multimethod-type-namer clojure.lang.PersistentVector [thing]
 "Vector")

 (defmethod multimethod-type-namer clojure.lang.PersistentArrayMap [thing]
    "Map")


(defmethod multimethod-type-namer java.lang.Long [thing]
        "default")

(defn things [thing]
    (str "Soy un " (multimethod-type-namer thing))
)

