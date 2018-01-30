package de.thm.arsnova.shared.mappings

import spray.json._
import de.thm.arsnova.shared.entities._
import de.thm.arsnova.shared.entities.export._

object EntitiesJsonProtocol extends DefaultJsonProtocol with UUIDFormat {
  implicit object FormatAttributesJsonProtocol extends RootJsonFormat[FormatAttributes] {
    def write(fA: FormatAttributes): JsValue = {
      val keyVals = fA.attributes.map {
        case (key, value) => key -> JsString(value)
      }
      JsObject(keyVals)
    }

    def read(json: JsValue): FormatAttributes = {
      json match {
        case js: JsObject => {
          val wat: Map[String, JsValue] = json.asJsObject.fields
          val attributes = wat.map {
            case (key, value) => key -> value.convertTo[String]
          }
          FormatAttributes(attributes = attributes)
        }
        case _ => FormatAttributes(Map())
      }
    }
  }
  
  implicit val answerOptionFormat: RootJsonFormat[AnswerOption] = jsonFormat5(AnswerOption.apply)
  implicit val choiceAnswerFormat: RootJsonFormat[ChoiceAnswer] = jsonFormat6(ChoiceAnswer.apply)
  implicit val commentFormat: RootJsonFormat[Comment] = jsonFormat7(Comment.apply)
  implicit val contentGroupFormat: RootJsonFormat[ContentGroup] = jsonFormat2(ContentGroup.apply)
  implicit val contentFormat: RootJsonFormat[Content] = jsonFormat16(Content.apply)
  implicit val freetextAnswerFormat: RootJsonFormat[FreetextAnswer] = jsonFormat6(FreetextAnswer.apply)
  implicit val roomFormat: RootJsonFormat[Room] = jsonFormat11(Room.apply)
  implicit val roundTransitionFormat: RootJsonFormat[RoundTransition] = jsonFormat5(RoundTransition.apply)
  implicit val tokenFormat: RootJsonFormat[Token] = jsonFormat5(Token.apply)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User.apply)
  implicit val choiceAnswerSummaryFormat: RootJsonFormat[ChoiceAnswerSummary] = jsonFormat2(ChoiceAnswerSummary.apply)

  // export protocols
  implicit val answerOptionExportFormat: RootJsonFormat[AnswerOptionExport] = jsonFormat4(AnswerOptionExport.apply)
  implicit val contentExportExportFormat: RootJsonFormat[ContentExport] = jsonFormat16(ContentExport.apply)
  implicit val freetextAnswerExportFormat: RootJsonFormat[FreetextAnswerExport] = jsonFormat2(FreetextAnswerExport.apply)
  implicit val choiceAnswerExportFormat: RootJsonFormat[ChoiceAnswerExport] = jsonFormat2(ChoiceAnswerExport.apply)
  implicit val roomExportFormat: RootJsonFormat[RoomExport] = jsonFormat5(RoomExport.apply)
}
