using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime
{
    /// <summary>
    /// This class implements the various vm::op options that are
    /// available.
    /// </summary>
    public static partial class Ops
    {
        /// <summary>
        /// Entry point to multi-dispatch over the current dispatchee list.
        /// </summary>
        /// <param name="TC"></param>
        /// <returns></returns>
        public static RakudoObject multi_dispatch_over_lexical_candidates(ThreadContext TC)
        {
            var CurOuter = TC.CurrentContext;
            while (CurOuter != null)
            {
                var CodeObj = CurOuter.StaticCodeObject;
                if (CodeObj.Dispatchees != null)
                {
                    var Candidate = MultiDispatch.MultiDispatcher.FindBestCandidate(
                        CodeObj, CurOuter.Capture);
                    return Candidate.STable.Invoke(TC, Candidate, CurOuter.Capture);
                }
                CurOuter = CurOuter.Outer;
            }
            throw new Exception("Could not find dispatchee list!");
        }

        /// <summary>
        /// Sets the dispatches of the given code object. Expects something with
        /// RakudoCodeRef and P6list representation respectively.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="CodeObject"></param>
        /// <param name="Dispatchees"></param>
        /// <returns></returns>
        public static RakudoObject set_dispatchees(ThreadContext TC, RakudoObject CodeObject, RakudoObject Dispatchees)
        {
            var Code = CodeObject as RakudoCodeRef.Instance;
            var DispatchList = Dispatchees as P6list.Instance;
            if (Code != null && DispatchList != null)
            {
                Code.Dispatchees = DispatchList.Storage.ToArray();
                return Code;
            }
            else
            {
                throw new Exception("set_dispatchees must be passed a RakudoCodeRef and a P6list.");
            }
        }

        /// <summary>
        /// Creates an instantiation of the dispatch routine (or proto, which may
        /// serve as one) supplied and augments it with the provided candidates.
        /// It relies on being passed the instantiation of the dispatcher from the
        /// last outer scope that had an instantiation, and we thus take its
        /// candidates. This may or may not hold up in the long run; it works out
        /// in the Perl 6-y "you can make a new instance from any object" sense
        /// though, and seems more likely to get the closure semantics right than
        /// any of the other approaches I've considered so far.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="ToInstantiate"></param>
        /// <param name="ExtraDispatchees"></param>
        /// <returns></returns>
        public static RakudoObject create_dispatch_and_add_candidates(ThreadContext TC, RakudoObject ToInstantiate, RakudoObject ExtraDispatchees)
        {
            // Make sure we got the right things.
            var Source = ToInstantiate as RakudoCodeRef.Instance;
            var AdditionalDispatchList = ExtraDispatchees as P6list.Instance;
            if (Source == null || AdditionalDispatchList == null)
                throw new Exception("create_dispatch_and_add_candidates expects a RakudoCodeRef and a P6list");

            // Clone all but SC (since it's a new object and doesn't live in any
            // SC yet) and dispatchees (which we want to munge).
            var NewDispatch = new RakudoCodeRef.Instance(Source.STable);
            NewDispatch.Body = Source.Body;
            NewDispatch.CurrentContext = Source.CurrentContext;
            NewDispatch.Handlers = Source.Handlers;
            NewDispatch.OuterBlock = Source.OuterBlock;
            NewDispatch.OuterForNextInvocation = Source.OuterForNextInvocation;
            NewDispatch.Sig = Source.Sig;
            NewDispatch.StaticLexPad = Source.StaticLexPad;

            // Take existing candidates and add new ones.
            NewDispatch.Dispatchees = new RakudoObject[Source.Dispatchees.Length + AdditionalDispatchList.Storage.Count];
            var i = 0;
            for (int j = 0; j < Source.Dispatchees.Length; j++)
                NewDispatch.Dispatchees[i++] = Source.Dispatchees[j];
            for (int j = 0; j < AdditionalDispatchList.Storage.Count; j++)
                NewDispatch.Dispatchees[i++] = AdditionalDispatchList.Storage[j];

            return NewDispatch;
        }

        /// <summary>
        /// Adds a single new candidate to the end of a dispatcher's candidate
        /// list.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Dispatcher"></param>
        /// <param name="Dispatchee"></param>
        /// <returns></returns>
        public static RakudoObject push_dispatchee(ThreadContext TC, RakudoObject Dispatcher, RakudoObject Dispatchee)
        {
            // Validate that we've got something we can push a new dispatchee on to.
            var Target = Dispatcher as RakudoCodeRef.Instance;
            if (Target == null)
                throw new Exception("push_dispatchee expects a RakudoCodeRef");
            if (Target.Dispatchees == null)
                throw new Exception("push_dispatchee passed something that is not a dispatcher");

            // Add it.
            var NewList = new RakudoObject[Target.Dispatchees.Length + 1];
            for (int i = 0; i < Target.Dispatchees.Length; i++)
                NewList[i] = Target.Dispatchees[i];
            NewList[Target.Dispatchees.Length] = Dispatchee;
            Target.Dispatchees = NewList;

            return Target;
        }

        /// <summary>
        /// Checks if a routine is considered a dispatcher (that is, if it has a
        /// candidate list).
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Check"></param>
        /// <returns></returns>
        public static RakudoObject is_dispatcher(ThreadContext TC, RakudoObject Check)
        {
            var Checkee = Check as RakudoCodeRef.Instance;
            if (Checkee != null && Checkee.Dispatchees != null)
                return Ops.box_int(TC, 1, TC.DefaultBoolBoxType);
            else
                return Ops.box_int(TC, 0, TC.DefaultBoolBoxType);
        }
    }
}
