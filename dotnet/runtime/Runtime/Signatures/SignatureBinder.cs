using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime
{
    /// <summary>
    /// Simple signature binder implementation.
    /// </summary>
    public static class SignatureBinder
    {
        /// <summary>
        /// Singleton empty positionals array.
        /// </summary>
        private static RakudoObject[] EmptyPos = new RakudoObject[0];

        /// <summary>
        /// Single empty nameds hash.
        /// </summary>
        private static Dictionary<string, RakudoObject> EmptyNamed = new Dictionary<string, RakudoObject>();

        /// <summary>
        /// Binds the capture against the given signature and stores the
        /// bound values into variables in the lexpad.
        /// 
        /// XXX No type-checking is available just yet. :-(
        /// 
        /// XXX No support for nameds mapping to positionals yet either.
        /// 
        /// (In other words, this kinda sucks...)
        /// </summary>
        /// <param name="C"></param>
        /// <param name="Capture"></param>
        public static void Bind(ThreadContext TC, Context C, RakudoObject Capture)
        {
            // Make sure the object is really a low level capture (don't handle
            // otherwise yet) and grab the pieces.
            var NativeCapture = Capture as P6capture.Instance;
            if (NativeCapture == null)
                throw new NotImplementedException("Can only deal with native captures at the moment");
            var Positionals = NativeCapture.Positionals ?? EmptyPos;
            var Nameds = NativeCapture.Nameds ?? EmptyNamed;
            Dictionary<string, bool> SeenNames = null;

            // See if we have to do any flattening.
            if (NativeCapture.FlattenSpec != null)
                Flatten(NativeCapture.FlattenSpec, ref Positionals, ref Nameds);

            // If we have no signature, that's same as an empty signature.
            var Sig = C.StaticCodeObject.Sig;
            if (Sig == null)
                return;

            // Current positional.
            var CurPositional = 0;

            // Iterate over the parameters.
            var Params = Sig.Parameters;
            var NumParams = Params.Length;
            for (int i = 0; i < NumParams; i++)
            {
                var Param = Params[i];

                // Positional required?
                if (Param.Flags == Parameter.POS_FLAG)
                {
                    if (CurPositional < Positionals.Length)
                    {
                        // We have an argument, just bind it.
                        C.LexPad.Storage[Param.VariableLexpadPosition] = Positionals[CurPositional];
                    }
                    else
                    {
                        throw new Exception("Not enough positional parameters; got " +
                            CurPositional.ToString() + " but needed " +
                            NumRequiredPositionals(C.StaticCodeObject.Sig).ToString());
                    }

                    // Increment positional counter.
                    CurPositional++;
                }

                // Positonal optional?
                else if (Param.Flags == Parameter.OPTIONAL_FLAG)
                {
                    if (CurPositional < Positionals.Length)
                    {
                        // We have an argument, just bind it.
                        C.LexPad.Storage[Param.VariableLexpadPosition] = Positionals[CurPositional];
                        CurPositional++;
                    }
                    else
                    {
                        // Default value, vivification.
                        // ((RakudoCodeRef.Instance)Param.DefaultValue).CurrentContext = TC.CurrentContext;
                        C.LexPad.Storage[Param.VariableLexpadPosition] = Param.DefaultValue.STable.Invoke(TC, Param.DefaultValue, Capture);
                    }
                }

                // Named slurpy?
                else if ((Param.Flags & Parameter.NAMED_SLURPY_FLAG) != 0)
                {
                    var SlurpyHolder = TC.DefaultHashType.STable.REPR.instance_of(TC, TC.DefaultHashType);
                    C.LexPad.Storage[Param.VariableLexpadPosition] = SlurpyHolder;
                    foreach (var Name in Nameds.Keys)
                        if (SeenNames == null || !SeenNames.ContainsKey(Name))
                            Ops.llmapping_bind_at_key(TC, SlurpyHolder,
                                Ops.box_str(TC, Name, TC.DefaultStrBoxType),
                                Nameds[Name]);
                }

                // Positional slurpy?
                else if ((Param.Flags & Parameter.POS_SLURPY_FLAG) != 0)
                {
                    var SlurpyHolder = TC.DefaultArrayType.STable.REPR.instance_of(TC, TC.DefaultArrayType);
                    C.LexPad.Storage[Param.VariableLexpadPosition] = SlurpyHolder;
                    int j;
                    for (j = CurPositional; j < Positionals.Length; j++)
                        Ops.lllist_push(TC, SlurpyHolder, Positionals[j]);
                    CurPositional = j;
                }

                // Named?
                else if (Param.Name != null)
                {
                    // Yes, try and get argument.
                    RakudoObject Value;
                    if (Nameds.TryGetValue(Param.Name, out Value))
                    {
                        // We have an argument, just bind it.
                        C.LexPad.Storage[Param.VariableLexpadPosition] = Value;
                        if (SeenNames == null)
                            SeenNames = new Dictionary<string, bool>();
                        SeenNames.Add(Param.Name, true);
                    }
                    else
                    {
                        // Optional?
                        if ((Param.Flags & Parameter.OPTIONAL_FLAG) == 0)
                        {
                            throw new Exception("Required named parameter " + Param.Name + " missing");
                        }
                        else
                        {
                            C.LexPad.Storage[Param.VariableLexpadPosition] = Param.DefaultValue.STable.Invoke(TC, Param.DefaultValue, Capture);
                        }
                    }
                }

                // Otherwise, WTF?
                else
                {

                }
            }

            // Ensure we had enough positionals.
            var PossiesInCapture = Positionals.Length;
            if (CurPositional > PossiesInCapture)
                throw new Exception("Too many positional arguments passed; expected " +
                    NumRequiredPositionals(C.StaticCodeObject.Sig).ToString() +
                    " but got " + PossiesInCapture.ToString());

            // XXX TODO; Ensure we don't have leftover nameds.
        }

        /// <summary>
        /// The number of positionals we require.
        /// </summary>
        /// <param name="Sig"></param>
        /// <returns></returns>
        private static int NumRequiredPositionals(Signature Sig)
        {
            int Num = 0;
            foreach (var Param in Sig.Parameters)
                if (Param.Flags != 0 || Param.Name != null)
                    break;
                else
                    Num++;
            return Num;
        }

        /// <summary>
        /// Flattens arguments into the positionals list or the nameds. This
        /// is pretty straightforward way, could be optimized in various ways,
        /// such as special case where we only have one arg which is the whole
        /// parameter set.
        /// </summary>
        /// <param name="FlattenSpec"></param>
        /// <param name="Positionals"></param>
        /// <param name="Naneds"></param>
        private static void Flatten(int[] FlattenSpec, ref RakudoObject[] Positionals, ref Dictionary<string, RakudoObject> Naneds)
        {
            // We'll build new positional and nameds.
            var NewPositionals = new List<RakudoObject>();
            var NewNameds = new Dictionary<string, RakudoObject>(Naneds);

            // Go through positional arguments and look for things to flatten.
            for (int i = 0; i < Positionals.Length; i++)
            {
                if (FlattenSpec[i] == CaptureHelper.FLATTEN_NONE)
                {
                    NewPositionals.Add(Positionals[i]);
                }
                else if (FlattenSpec[i] == CaptureHelper.FLATTEN_POS)
                {
                    // XXX For now rely on it being a P6list but in the future we
                    // should handle other cases.
                    var Flattenee = Positionals[i] as P6list.Instance;
                    if (Flattenee != null)
                    {
                        NewPositionals.AddRange(Flattenee.Storage);
                    }
                    else
                    {
                        throw new InvalidOperationException("Currently can only flatten a P6list");
                    }
                }
                else if (FlattenSpec[i] == CaptureHelper.FLATTEN_NAMED)
                {
                    // XXX For now rely on it being a P6mapping but in the future we
                    // should handle other cases.
                    var Flattenee = Positionals[i] as P6mapping.Instance;
                    if (Flattenee != null)
                    {
                        foreach (var Pair in Flattenee.Storage)
                            NewNameds.Add(Pair.Key, Pair.Value);
                    }
                    else
                    {
                        throw new InvalidOperationException("Currently can only flatten a P6mapping");
                    }
                }
            }

            // Put updated positionals and nameds in place.
            Positionals = NewPositionals.ToArray();
            Naneds = NewNameds;
        }
    }
}
