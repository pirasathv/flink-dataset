package com.kinaxis.expedition.model.in

case class Expedition(
    expeditionNumber: Int,
    expeditionType: String,
    tripNumber: Int,
    quantity: Int,
    mineral: String,
    price: Int
)
