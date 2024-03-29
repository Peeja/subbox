(ns subbox.styles
  (:require [garden.color :refer [rgba]]
            [garden.def :refer [defstyles]]
            [garden.units :refer [px percent]]))


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

(defstyles heading-font
  {:font {:family "Nunito, sans-serif"
          :weight 400}})

(defstyles screen
  [:body
   {:font {:family "Open Sans, sans-serif"
           :weight 400
           :size (px 16)}}

   [:>header
    {:padding (px 10)}
    [:>h1
     heading-font
     {:float :left
      :font-size (px 32)}]
    [:>a.logout
     {:display :block
      :float :right }]]]

  [:.app
   {:position :absolute
    :top (px 50)
    :bottom 0
    :right 0
    :left 0}

   [:>div.no-subscriptions-found
    {:margin (px 10)}]

   [:>ul.subscriptions
    block-list
    {:position :absolute
     :top 0
     :bottom 0
     :left 0
     :width subscriptions-bar-width
     :overflow :auto}

    [:>li
     {:cursor :pointer
      :height (px 50)}
     [:&.selected
      {:color :blue}]
     [:>img.thumbnail
      (let [size (px 40)]
        {:width size
         :height size
         :margin (px 10)
         :vertical-align :middle})]
     [:>span.title
      {:vertical-align :middle}]]]

   [:>section.main
    {:position :absolute
     :top 0
     :bottom 0
     :left subscriptions-bar-width
     :right 0
     :overflow :auto}

    [:>h1
     heading-font
     {:font-size (px 64)
      :margin {:bottom (px 40)}}]

    [:>ul.videos
     block-list
     [:>li
      {:cursor :pointer
       :margin {:bottom (px 40)}}
      [:article.video
       clearfix
       [:&.watched
        {:opacity 0.5}]
       [:>img.thumbnail
        {:float :left
         :margin {:right (px 20)}}]
       [:>div.info
        {:float :left
         :width (px 500)}
        [:>.title
         heading-font
         {:font-size (px 32)
          :line-height (px 28)
          :margin {:bottom (px 20)}}]
        [:>.description
          {:line-height (px 20)}
         [:>p
          {:margin {:bottom (px 10)}}]]]]]]]

   [:>.watch-screen
    {:position :absolute
     :z-index 10
     :top 0
     :bottom 0
     :left 0
     :right 0
     :background (rgba 0 0 0 0.7)}
    [:>iframe
     {:position :absolute
      :top 0
      :bottom 0
      :left 0
      :right 0
      :width (px 960)
      :height (px 585)
      :margin :auto}]]])
