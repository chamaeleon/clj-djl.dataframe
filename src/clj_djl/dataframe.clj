(ns clj-djl.dataframe
  (:require
   [clj-djl.ndarray :as nd]
   [tech.v3.dataset :as ds]
   [tech.v3.datatype.export-symbols :refer [export-symbols]])
  (:refer-clojure
   :exclude
   [filter group-by sort-by concat take-nth shuffle rand-nth update])
  (:import ai.djl.ndarray.NDArray))

(export-symbols tech.v3.dataset
                row-count
                column-count
                column
                columns
                column-names
                has-column?
                columns-with-missing-seq
                add-column
                new-column
                remove-column
                remove-columns
                drop-columns
                update-column
                order-column-names
                update-columns
                rename-columns
                select
                select-by-index
                unordered-select
                select-columns
                select-columns-by-index
                select-rows
                select-rows-by-index
                drop-rows
                remove-rows
                missing
                drop-missing
                add-or-update-column
                assoc-ds
                group-by->indexes
                group-by-column->indexes
                group-by
                group-by-column
                sort-by
                sort-by-column
                filter
                filter-column
                unique-by
                unique-by-column
                concat
                concat-copying
                concat-inplace
                take-nth
                ensure-array-backed
                brief
                categorical->one-hot
                replace-missing
                head
                tail)

(def ->dataframe ds/->dataset)
(def dataframe-name ds/dataset-name)
(def set-dataframe-name ds/set-dataset-name)
(def dataframe->data ds/dataset->data)
(def data->dataframe ds/data->dataset)


(defn ->ndarray
  "Convert dataframe to NDArray"
  [ndm dataframe]
  (nd/t (nd/create ndm (map vec (ds/columns dataframe)))))

(defn ->dataframe
  ([dataframe
    {:keys [table-name dataset-name]
     :as options}]
   (let [dataframe
         (if (instance? NDArray dataframe)
           (if (= 2 (count (nd/to-vec (nd/shape dataframe))))
             (zipmap (range ((nd/to-vec (nd/shape dataframe)) 1))
                     (map vec
                          (partition ((nd/to-vec (nd/shape dataframe)) 0)
                                     (nd/to-vec (nd/t dataframe)))))
             (throw (java.lang.IllegalArgumentException. "Can not convert ndarray (dim != 2) to dataframe!")))
           dataframe)]
     (ds/->dataset dataframe options)))
  ([dataframe]
   (->dataframe dataframe {})))

(defn shape
  "Get the shape of dataframe, in row major way"
  [dataframe]
  (vec (reverse (ds/shape dataframe))))


(defn select-by-index
  "Select a sub-dataframe by seq of row index and column index"
  [dataframe row-index col-index]
  (ds/select-by-index dataframe col-index row-index))


(defn replace-missing
  "Replace missing with:

  - builtin strategys: `:mid` `:up` `:down` and `:lerp`
  - value
  - or column function with missing slot dropped"
  ([df]
   (ds/replace-missing df))
  ([df strategy]
   (replace-missing df :all strategy))
  ([df col-sel strategy]
   (cond
     ;; one of the builtin strategies
     (contains? #{:mid :up :down :lerp} strategy)
     (ds/replace-missing df col-sel strategy)
     :else
     (ds/replace-missing df col-sel :value strategy))))
