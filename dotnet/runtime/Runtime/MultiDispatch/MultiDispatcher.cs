using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime.MultiDispatch
{
    /// <summary>
    /// Very first cut implementation of a multi-dispatcher. Doesn't yet
    /// know about subtyping beyond no type being the top type. Yes, this
    /// will likely get replaced (or extensively re-done) at some point.
    /// </summary>
    public static class MultiDispatcher
    {
        /// <summary>
        /// Finds the best candidate, if one exists, and returns it.
        /// </summary>
        /// <param name="Candidates"></param>
        /// <param name="Capture"></param>
        /// <returns></returns>
        public static RakudoCodeRef.Instance FindBestCandidate(RakudoCodeRef.Instance DispatchRoutine, RakudoObject Capture)
        {
            // Sort the candidates.
            // XXX Cache this in the future.
            var SortedCandidates = Sort(DispatchRoutine.Dispatchees);

            // Extract the native capture.
            // XXX Handle non-native captures too.
            var NativeCapture = Capture as P6capture.Instance;

            // Now go through the sorted candidates and find the first one that
            // matches.
            var PossiblesList = new List<RakudoCodeRef.Instance>();
            foreach (RakudoCodeRef.Instance Candidate in SortedCandidates)
            {
                // If we hit a null, we're at the end of a group.
                if (Candidate == null)
                {
                    if (PossiblesList.Count == 1)
                        return PossiblesList[0];
                    else if (PossiblesList.Count > 1)
                        // Here is where you'd handle constraints.
                        throw new Exception("Ambiguous dispatch: more than one candidate matches");
                    else
                        continue;
                }
                
                /* Check if it's admissable by arity. */
                var NumArgs = NativeCapture.Positionals.Length;
                if (NumArgs < Candidate.Sig.NumRequiredPositionals ||
                    NumArgs > Candidate.Sig.NumPositionals)
                    continue;

                /* Check if it's admissable by type. */
                var TypeCheckCount = Math.Min(NumArgs, Candidate.Sig.NumPositionals);
                var TypeMismatch = false;
                for (int i = 0; i < TypeCheckCount; i++) {
                    var Arg = NativeCapture.Positionals[i];
                    var Type = Candidate.Sig.Parameters[i].Type;
                    if (Arg.STable.WHAT != Type && Type != null)
                    {
                        TypeMismatch = true;
                        break;
                    }
                }
                if (TypeMismatch)
                    continue;

                /* If we get here, it's an admissable candidate; add to list. */
                PossiblesList.Add(Candidate);
            }

            // If we get here, no candidates matched.
            throw new Exception("No candidates found to dispatch to");
        }

        /// <summary>
        /// Sorts the candidates.
        /// </summary>
        /// <param name="Unsorted"></param>
        /// <returns></returns>
        private static List<RakudoObject> Sort(RakudoObject[] Unsorted)
        {
            var Sorted = new List<RakudoObject>(Unsorted);
            Sorted.Add(null);
            return Sorted;
        }

        /// <summary>
        /// Checks if one signature is narrower than another.
        /// </summary>
        /// <param name="a"></param>
        /// <param name="b"></param>
        /// <returns></returns>
        private static int IsNarrower(RakudoCodeRef.Instance a, RakudoCodeRef.Instance b)
        {
            var Narrower = 0;
            var Tied = 0;
            int i, TypesToCheck;

            /* Work out how many parameters to compare, factoring in slurpiness
             * and optionals. */
            if (a.Sig.NumPositionals == b.Sig.NumPositionals)
                TypesToCheck = a.Sig.NumPositionals;
            else if (a.Sig.NumRequiredPositionals == b.Sig.NumRequiredPositionals)
                TypesToCheck = Math.Min(a.Sig.NumPositionals, b.Sig.NumPositionals);
            else
                return 0;

            /* Analyse each parameter in the two candidates. */
            for (i = 0; i < TypesToCheck; i++) {
                var TypeObjA = a.Sig.Parameters[i].Type;
                var TypeObjB = b.Sig.Parameters[i].Type;
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
        public static bool IsNarrowerType(RakudoObject A, RakudoObject B)
        {
            // For now, differentiate any (represented by null) and a type.
            if (B != null && A == null)
                return false;
            else
                return true;
        }
    }
}
