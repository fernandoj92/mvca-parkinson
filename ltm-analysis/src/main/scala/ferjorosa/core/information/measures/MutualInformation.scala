package ferjorosa.core.information.measures

import java.util

import org.latlab.clustering.EmpiricalMiComputerForBinaryData
import org.latlab.util.{DataSet, Variable}

import scala.collection.convert.WrapAsJava

/**
  * Created by equipo on 13/01/2017.
  */
// Discrete
// La idea de los DataCases solo nos vale para los DataSets formados unicamente por variables discretas
// TODO: Uses WrapAsJava
object MutualInformation {

  def computePairWise(variables: Seq[Variable], dataSet: DataSet): Map[(Variable, Variable), Double] = {

    if(variables.size < 2)
      throw new IllegalArgumentException("2 variables minimum")

    if(absentVariables(variables, dataSet))
      throw new IllegalArgumentException("Cannot calculate the MI of a variable that is not present in the DataSet")

    /**
      * Returns a matrix with the pairwise MI scores, where the diagonal contains invalid values
      */
    val miCalculator: EmpiricalMiComputerForBinaryData = new EmpiricalMiComputerForBinaryData(dataSet, WrapAsJava.seqAsJavaList(variables))
    val miMatrix: util.ArrayList[Array[Double]] = miCalculator.computerPairwise

    // Recorremos la matrix triangular inferior para reducir el numero de iteraciones y creamos un Pair de Variables segun los indices
    // asignando a dicho Pair su MI value.

    (1 until miMatrix.size()).flatMap{ row =>
      for(col <- 0 until row)
        yield (dataSet.getVariables.apply(col), dataSet.getVariables.apply(row)) -> miMatrix.get(row).apply(col)
    }.toMap
  }

  def computePairWise(vX: Variable, vY: Variable, dataSet: DataSet): Double = {

    val logBase = 2 // Bivariate MI



    ???
  }

  private def absentVariables(variables: Seq[Variable], dataSet: DataSet): Boolean = {
    dataSet.getVariables.map(variables.contains(_)).count(_ == false) > 0
  }

}
