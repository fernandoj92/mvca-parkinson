package ferjorosa.examples.islandFinder

import ferjorosa.core.information.measures.MutualInformation
import org.latlab.util.{DataSet, DataSetLoader}

/**
  * Created by equipo on 13/01/2017.
  */
object MI_Example {

  def main(args: Array[String]): Unit = {

    val data: DataSet = new DataSet(DataSetLoader.convert("data/Asia_train.arff"))

    val mutualInformation = MutualInformation.computePairWise(data.getVariables, data)
    //val x = 0


  }
}
