package com.kinaxis.expedition.util

import java.io.File
import java.util

import org.apache.flink.api.java.utils.ParameterTool

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.Try

/** Config priority order :
  *  1. program args
  *  2. system properties
  *  3. application.properties
  *  4. reference.properties
  */
class Config(
    args: Array[String],
    applicationFilename: String = "application.properties",
    referenceFilename: String = "reference.properties"
) {

  private val applicationFileAsMap = getPropertyFileAsJavaMap(applicationFilename)
  private val referenceFileAsMap = getPropertyFileAsJavaMap(referenceFilename)

  val argsParameters: ParameterTool = ParameterTool.fromArgs(args)
  val systemPropertiesParameters: ParameterTool = ParameterTool.fromSystemProperties()
  val applicationFileParameters: Option[ParameterTool] = applicationFileAsMap.map(ParameterTool.fromMap)
  val referenceFileParameters: Option[ParameterTool] = referenceFileAsMap.map(ParameterTool.fromMap)

  private def get(key: String): String =
    if (argsParameters.has(key))
      argsParameters.get(key)
    else if (systemPropertiesParameters.has(key))
      systemPropertiesParameters.get(key)
    else if (applicationFileParameters.exists(_.has(key)))
      applicationFileParameters.get.get(key)
    else if (referenceFileParameters.exists(_.has(key)))
      referenceFileParameters.get.get(key)
    else throw new RuntimeException(s"No data for required key '$key'")

  def getString(key: String): String = get(key)

  def getInt(key: String): Int = get(key).toInt

  def getLong(key: String): Long = get(key).toLong

  def getBoolean(key: String): Boolean = get(key).toBoolean

  private def getPropertyFileAsJavaMap(filename: String): Option[util.Map[String, String]] =
    Try(Source.fromResource(filename).getLines()).toOption
      .map(
        _.map(_.trim)
          .filter(!_.startsWith("#"))
          .filter(_.contains("="))
          .map(_.split("="))
          .map(property => property.head.trim -> property.last.trim)
          .toMap
      )
      .map(mapAsJavaMap)

}

object Config {

  @deprecated("use new Config(args) instead")
  def apply(args: Array[String]): ParameterTool =
    if (!args.isEmpty)
      ParameterTool.fromArgs(args)
    else
      ParameterTool.fromPropertiesFile(new File("src/main/resources/application.properties"))

}
