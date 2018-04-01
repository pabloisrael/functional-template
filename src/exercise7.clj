
(ns exercise7)

   
(defn mapHash
    [f m]
    (into (empty m) (for [[k v] m] [k (f v)])))
    
(defn fmap
    [function list]
    (if (map? list) (mapHash function list) (map function list)))
     