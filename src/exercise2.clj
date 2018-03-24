(ns exercise2)

(defn only-greater-than-five
	[list]
	(filter #(> % 5) list))
