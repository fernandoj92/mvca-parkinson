package voltric.variables;

import java.util.List;

/**
 * Created by equipo on 17/02/2017.
 */

// TODO; la solucion no es la factoria, es encontrar una soluction al problema de la covarianza de List, mientras se resuleve
    // el problema del type erasure en el constructor.
	//TODO: Creo que la solución es hacer la clase generica, y sino generar varios tipos de colecciones de variables y generar una nuvea jerarquia.
public class VariableCollectionFactory {
/*
    public static VariableCollection createFromGeneric(List<Variable> variables){
        return new VariableCollection(variables);
    }

    public static VariableCollection createFromDiscrete(List<DiscreteVariable> variables){
        List<Variable> genericVariableList = variables;
        return new VariableCollection(variables);
    }

    public static VariableCollection createFromConinuous(List<ContinuousVariable> variables){
        return new VariableCollection(variables);
    }*/
}
