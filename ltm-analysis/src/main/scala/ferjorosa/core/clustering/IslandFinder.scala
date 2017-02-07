package ferjorosa.core.clustering

import java.util

import ferjorosa.core.information.measures.MutualInformation
import org.latlab.model.LTM
import org.latlab.util.{DataSet, Variable}

import scala.collection.convert.WrapAsJava

/**
  * Created by equipo on 13/01/2017.
  */
case class IslandFinder(emThreshold: Double,
                        emMaxSteps: Int,
                        emNumRestarts: Int,
                        maxIslands: Int,
                        udThreshold: Double) {

  def find(dataSet: DataSet): util.Collection[LTM] = {

    val variables: Array[Variable] = dataSet.getVariables
    val mutualInformationPairs = MutualInformation.computePairWise(variables, dataSet)

    while(!isDone){

      if(variables.length == 3) {
        val bestPair = findBestPair(mutualInformationPairs)
        // TODO: Making a conversion to Java ArrayList
        val javaArrayList = new java.util.ArrayList[Variable]()
        variables.map(javaArrayList.add(_))
        dataSet.project(javaArrayList)
      }

    }

    ???
  }

  def isDone: Boolean = {
    true
  }

  /**
    * Learns a three-MVs LCM
    * @param variables
    * @param data_proj
    * @return
    */
  private def LCM3N(variables: java.util.ArrayList[Variable], data_proj: DataSet): LTM = {

    ???
  }

  private def findBestPair(pairMIScores: Map[(Variable, Variable), Double]): (Variable, Variable) = {
    pairMIScores.maxBy(_._2)._1
  }

}
