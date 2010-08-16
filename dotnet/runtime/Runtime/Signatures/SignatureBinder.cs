using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;

namespace Rakudo.Runtime
{
    /// <summary>
    /// Simple signature binder implementation.
    /// </summary>
    public static class SignatureBinder
    {
        /// <summary>
        /// Binds the capture against the given signature and stores the
        /// bound values into variables in the lexpad.
        /// 
        /// XXX No type-checking is available just yet. :-(
        /// 
        /// XXX No proper handling of optionals and defaults yet.
        /// 
        /// XXX No support for nameds mapping to positionals yet either.
        /// 
        /// (In other words, this kinda sucks...)
        /// </summary>
        /// <param name="C"></param>
        /// <param name="Capture"></param>
        public static void Bind(Context C, IRakudoObject Capture)
        {
            // The lexpad we'll bind into.
            var Target = C.LexPad;

            // Current positional.
            var CurPositional = 0;

            // Iterate over the parameters.
            var Params = C.StaticCodeObject.Sig.Parameters;
            foreach (var Param in Params)
            {
                // Named slurpy?
                if ((Param.Flags & Parameter.NAMED_SLURPY_FLAG) != 0)
                {
                    throw new Exception("Named slurpy parameters are not yet implemented.");
                }

                // Named positional?
                else if ((Param.Flags & Parameter.POS_SLURPY_FLAG) != 0)
                {
                    throw new Exception("Positional slurpy parameters are not yet implemented.");
                }

                // Named?
                else if (Param.Name != null)
                {
                    // Yes, try and get argument.
                    var Value = CaptureHelper.GetNamed(Capture, Param.Name);
                    if (Value != null)
                    {
                        // We have an argument, just bind it.
                        Target[Param.VariableName] = Value;
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
                            // XXX Default value, vivification.
                        }
                    }
                }

                // Otherwise, it's a positional.
                else
                {
                    var Value = CaptureHelper.GetPositional(Capture, CurPositional);
                    if (Value != null)
                    {
                        // We have an argument, just bind it.
                        Target[Param.VariableName] = Value;
                    }
                    else
                    {
                        // Optional?
                        if ((Param.Flags & Parameter.OPTIONAL_FLAG) == 0)
                        {
                            throw new Exception("Not enough positional parameters; got " +
                                CurPositional.ToString() + " but needed " +
                                NumRequiredPositionals(C.StaticCodeObject.Sig).ToString());
                        }
                        else
                        {
                            // XXX Default value, vivification.
                        }
                    }

                    // Increment positional counter.
                    CurPositional++;
                }
            }

            // Ensure we had enough positionals.
            var PossiesInCapture = CaptureHelper.NumPositionals(Capture);
            if (CurPositional != PossiesInCapture)
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
    }
}
