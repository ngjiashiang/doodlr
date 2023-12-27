package com.doodlr.models
import com.doodlr.protocol.JsonSerializable
import akka.actor.typed.ActorRef
import com.doodlr.actors.Client

case class User(name: String, ref: ActorRef[Client.Command]) extends JsonSerializable {
  override def toString: String = {
    name
  }
}
