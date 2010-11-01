package Rakudo.Runtime.MultiDispatch;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.P6capture;
import Rakudo.Metamodel.Representations.RakudoCodeRef;
import Rakudo.Runtime.DefinednessConstraint;

/// <summary>
/// Very first cut implementation of a multi-dispatcher. Doesn't yet
/// know about subtyping beyond no type being the top type. Yes, this
/// will likely get replaced (or extensively re-done) at some point.
/// </summary>
public class MultiDispatcher
{
    /// <summary>
    /// Finds the best candidate, if one exists, and returns it.
    /// </summary>
    /// <param name="Candidates"></param>
    /// <param name="Capture"></param>
    /// <returns></returns>
    public static RakudoObject FindBestCandidate(RakudoCodeRef.Instance dispatchRoutine, RakudoObject capture)
    {
        // Extract the native capture.
        // XXX Handle non-native captures too.
        P6capture.Instance nativeCapture = (P6capture.Instance)capture;

        // First, try the dispatch cache.
        if (dispatchRoutine.MultiDispatchCache != null && nativeCapture.Nameds == null)
        {
            RakudoObject cacheResult = dispatchRoutine.MultiDispatchCache.Lookup(nativeCapture.Positionals);
            if (cacheResult != null)
                return cacheResult;
        }

        // Sort the candidates.
        // XXX Cache this in the future.
        ArrayList<RakudoObject> sortedCandidates = candidateSort(dispatchRoutine.Dispatchees);

        // Now go through the sorted candidates and find the first one that
        // matches.
        ArrayList<RakudoCodeRef.Instance> possiblesList = new ArrayList<RakudoCodeRef.Instance>();
        for (RakudoObject candidateObject : sortedCandidates)
        {
            // TODO: remove yukky type cast
            RakudoCodeRef.Instance candidate = (RakudoCodeRef.Instance)candidateObject;
            // If we hit a null, we're at the end of a group.
            if (candidate == null)
            {
                if (possiblesList.size() == 1)
                {
                    // We have an unambiguous first candidate. Cache if possible and
                    // return it.
                    if (nativeCapture.Nameds == null)
                    {
                        if (dispatchRoutine.MultiDispatchCache == null)
                            dispatchRoutine.MultiDispatchCache = new DispatchCache();
                        dispatchRoutine.MultiDispatchCache.Add(nativeCapture.Positionals, possiblesList.get(0));
                    }
                    return possiblesList.get(0);
                }
                else if (possiblesList.size() > 1)
                {
                    // Here is where you'd handle constraints.
                    throw new UnsupportedOperationException("Ambiguous dispatch: more than one candidate matches");
                }
                else
                {
                    continue;
                }
            }
            
            /* Check if it's admissable by arity. */
            int numArgs = nativeCapture.Positionals.length;
            if (numArgs < candidate.Sig.NumRequiredPositionals ||
                numArgs > candidate.Sig.NumPositionals)
                continue;

            /* Check if it's admissable by type. */
            int typeCheckCount = Math.min(numArgs, candidate.Sig.NumPositionals);
            boolean typeMismatch = false;
            for (int i = 0; i < typeCheckCount; i++) {
                RakudoObject arg = nativeCapture.Positionals[i];
                RakudoObject type = candidate.Sig.Parameters[i].Type;
                if (arg.getSTable().WHAT != type && type != null)
                {
                    typeMismatch = true;
                    break;
                }
                DefinednessConstraint definedness = candidate.Sig.Parameters[i].Definedness;
                if (definedness != DefinednessConstraint.None)
                {
                    boolean argDefined = arg.getSTable().REPR.defined(null, arg);
                    if (definedness == DefinednessConstraint.DefinedOnly && !argDefined ||
                        definedness == DefinednessConstraint.UndefinedOnly && argDefined)
                    {
                        typeMismatch = true;
                        break;
                    }
                }
            }
            if (typeMismatch)
                continue;

            /* If we get here, it's an admissable candidate; add to list. */
            possiblesList.add(candidate);
        }

        // If we get here, no candidates matched.
        throw new UnsupportedOperationException("No candidates found to dispatch to");
    }

    /// <summary>
    /// Sorts the candidates.
    /// </summary>
    /// <param name="Unsorted"></param>
    /// <returns></returns>
    private static ArrayList<RakudoObject> candidateSort(RakudoObject[] unsorted)
    {
        ArrayList<RakudoObject> sorted = new ArrayList<RakudoObject>();
        for (RakudoObject obj : unsorted) {
            sorted.add(obj);
        }
        sorted.add(null); // XXX does this akshually sort?
        return sorted;
    }

    /// <summary>
    /// Checks if one signature is narrower than another.
    /// </summary>
    /// <param name="a"></param>
    /// <param name="b"></param>
    /// <returns></returns>
    private static int IsNarrower(RakudoCodeRef.Instance a, RakudoCodeRef.Instance b)
    {
        int Narrower = 0;
        int Tied = 0;
        int i, TypesToCheck;

        /* Work out how many parameters to compare, factoring in slurpiness
         * and optionals. */
        if (a.Sig.NumPositionals == b.Sig.NumPositionals)
            TypesToCheck = a.Sig.NumPositionals;
        else if (a.Sig.NumRequiredPositionals == b.Sig.NumRequiredPositionals)
            TypesToCheck = Math.min(a.Sig.NumPositionals, b.Sig.NumPositionals);
        else
            return 0;

        /* Analyse each parameter in the two candidates. */
        for (i = 0; i < TypesToCheck; i++) {
            RakudoObject TypeObjA = a.Sig.Parameters[i].Type;
            RakudoObject TypeObjB = b.Sig.Parameters[i].Type;
            if (TypeObjA == TypeObjB)
            {
                /* In a full Perl 6 multi dispatcher, you'd consider
                 * constraints here. */
                Tied++;
            }
            else
            {
                if (IsNarrowerType(TypeObjA, TypeObjB))
                    Narrower++;
                else if (!IsNarrowerType(TypeObjB, TypeObjA))
                    Tied++;
            }
        }

        /* If one is narrower than the other from current analysis, we're done. */
        if (Narrower >= 1 && Narrower + Tied == TypesToCheck)
            return 1;

        /* If they aren't tied, we're also done. */
        else if (Tied != TypesToCheck)
            return 0;

        /* Otherwise, we see if one has a slurpy and the other not. A lack of
        * slurpiness makes the candidate narrower. Otherwise, they're tied. */
        return !a.Sig.HasSlurpyPositional() && b.Sig.HasSlurpyPositional() ? 1 : 0;
    }

    /// <summary>
    /// Compares two types to see if the first is narrower than the second.
    /// XXX This is not complete yet, just very basic check.
    /// </summary>
    /// <returns></returns>
    public static boolean IsNarrowerType(RakudoObject A, RakudoObject B)
    {
        // For now, differentiate any (represented by null) and a type.
        if (B != null && A == null)
            return false;
        else
            return true;
    }
}

