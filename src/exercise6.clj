
(ns exercise6)


(defn faverage
	[list]
	(/ (reduce + list) (count list)))