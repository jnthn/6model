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
        public static RakudoCodeRef.Instance FindBestCandidate(List<RakudoCodeRef.Instance> Candidates, RakudoObject Capture)
        {
            throw new NotImplementedException();
        }
    }
}
