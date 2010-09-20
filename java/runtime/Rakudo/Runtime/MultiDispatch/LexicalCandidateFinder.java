package Rakudo.Runtime.MultiDispatch;

import java.util.ArrayList;

import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.P6list;
import Rakudo.Metamodel.Representations.RakudoCodeRef;
import Rakudo.Runtime.Context;

/// <summary>
/// Finds all candidates that we may dispatch to.
/// </summary>
public class LexicalCandidateFinder
{
    /// <summary>
    /// Locates all matching candidates between the two scopes.
    /// </summary>
    /// <param name="FromScope"></param>
    /// <param name="ToScope"></param>
    /// <param name="Name"></param>
    /// <returns></returns>
    public static ArrayList<RakudoCodeRef.Instance> FindCandidates(Context callerScope, Context protoScope, String candidateHolderName)
    {
        ArrayList<RakudoCodeRef.Instance> result = new ArrayList<RakudoCodeRef.Instance>();
        Context curScope = null;
        do
        {   // Get the next outer scope, or alternatively start off with the
            // caller scope.  (parenthesized for clarity).
            curScope = (curScope == null) ? callerScope : curScope.Outer;
            if (curScope == null)
                break;

            // Any candidates here?
            if (curScope.LexPad.SlotMapping != null && curScope.LexPad.SlotMapping.containsKey(candidateHolderName)) {
                Integer index = curScope.LexPad.SlotMapping.get(candidateHolderName);
                P6list.Instance p6listInstance = (P6list.Instance)curScope.LexPad.Storage[index];
                for (RakudoObject candidate : p6listInstance.Storage) {
                    result.add((RakudoCodeRef.Instance)candidate);
                }
            }
        } while (curScope != protoScope);
        return result;
    }
}
