(ns subbox.styles
  (:require [garden.def :refer [defstyles]]
            [garden.units :refer [px]]))


(def subscriptions-bar-width (px 300))


(defstyles block-list
  {:padding 0}
  [:>li
   {:list-style-type :none}])

(defstyles clearfix
  [:&:after
   {:content "\"\""
    :display :table
    :clear   :both}])

(defstyles screen
  [:.app

   [:>ul.subscriptions
    block-list
    {:position :absolute
     :top 0
     :bottom 0
     :left 0
     :width subscriptions-bar-width}

    [:>li
     {:cursor :pointer
      :height (px 40)}
     [:&.selected
      {:color :blue}]
     [:>img.thumbnail
      (let [size (px 30)]
        {:width size
         :height size
         :margin (px 5)
         :vertical-align :middle})]
     [:>span.title
      {:vertical-align :middle}]]]

   [:>section.main
    {:position :absolute
     :top 0
     :bottom 0
     :left subscriptions-bar-width
     :right 0}

    [:>ul.videos
     block-list
     [:>li
      [:article.video
       clearfix
       [:>img.thumbnail
        {:float :left}]
       [:>div.info
        {:float :left
         :width (px 200)}]]]]]])
