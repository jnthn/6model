package Rakudo.Runtime;

import java.util.HashMap;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.P6capture;
import Rakudo.Runtime.Context;

/// <summary>
/// Simple signature binder implementation.
/// </summary>
public class SignatureBinder // static class in the C# version
{
    /// <summary>
    /// Singleton empty positionals array.
    /// </summary>
    private static RakudoObject[] EmptyPos = new RakudoObject[0];

    /// <summary>
    /// Single empty nameds hash.
    /// </summary>
    private static HashMap<String, RakudoObject> EmptyNamed = new HashMap<String, RakudoObject>();

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
    public static void Bind(Context C, RakudoObject Capture)
    {
        // Make sure the object is really a low level capture (don't handle
        // otherwise yet) and grab the pieces.
        P6capture.Instance NativeCapture = (P6capture.Instance)Capture;
        if (NativeCapture == null)
            throw new UnsupportedOperationException("Can only deal with native captures at the moment");
        RakudoObject[] Positionals = NativeCapture.Positionals != null ? NativeCapture.Positionals : EmptyPos;
        HashMap<String,RakudoObject> Nameds = NativeCapture.Nameds != null ? NativeCapture.Nameds : EmptyNamed;

        // If we have no signature, that's same as an empty signature.
        Signature Sig = C.StaticCodeObject.Sig;
        if (Sig == null)
            return;

        // Current positional.
        int CurPositional = 0;

        // Iterate over the parameters.
        Parameter[] Params = Sig.Parameters;
        int NumParams = Params.length;
        for (int i = 0; i < NumParams; i++)
        {
            Parameter Param = Params[i];

            // Positional required?
            if (Param.Flags == Parameter.POS_FLAG)
            {
                if (CurPositional < Positionals.length)
                {
                    // We have an argument, just bind it.
                    C.LexPad.Storage[Param.VariableLexpadPosition] = Positionals[CurPositional];
                }
                else
                {
                    throw new UnsupportedOperationException("Not enough positional parameters; got " +
                        Integer.toString(CurPositional) + " but needed " +
                        Integer.toString(NumRequiredPositionals(C.StaticCodeObject.Sig)));
                }

                // Increment positional counter.
                CurPositional++;
            }

            // Positonal optional?
            else if (Param.Flags == Parameter.OPTIONAL_FLAG)
            {
                if (CurPositional < Positionals.length)
                {
                    // We have an argument, just bind it.
                    C.LexPad.Storage[Param.VariableLexpadPosition] = Positionals[CurPositional];
                }
                else
                {
                    // XXX Default value, vivification.
                }

                // Increment positional counter.
                CurPositional++;
            }

            // Named slurpy?
            else if ((Param.Flags & Parameter.NAMED_SLURPY_FLAG) != 0)
            {
                throw new UnsupportedOperationException("Named slurpy parameters are not yet implemented.");
            }

            // Named positional?
            else if ((Param.Flags & Parameter.POS_SLURPY_FLAG) != 0)
            {
                throw new UnsupportedOperationException("Positional slurpy parameters are not yet implemented.");
            }

            // Named?
            else if (Param.Name != null)
            {
                // Yes, try and get argument.
                if (Nameds.containsKey(Param.Name))
                {
                    // We have an argument, just bind it.
                    RakudoObject Value = Nameds.get(Param.Name);
                    C.LexPad.Storage[Param.VariableLexpadPosition] = Value;
                }
                else
                {
                    // Optional?
                    if ((Param.Flags & Parameter.OPTIONAL_FLAG) == 0)
                    {
                        throw new UnsupportedOperationException("Required named parameter " + Param.Name + " missing");
                    }
                    else
                    {
                        // XXX Default value, vivification.
                    }
                }
            }

            // Otherwise, WTF?
            else
            {

            }
        }

        // Ensure we had enough positionals.
        int PossiesInCapture = Positionals.length;
        if (CurPositional != PossiesInCapture)
            throw new UnsupportedOperationException("Too many positional arguments passed; expected " +
                Integer.toString(NumRequiredPositionals(C.StaticCodeObject.Sig)) +
                " but got " + Integer.toString(PossiesInCapture));

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
        for (Parameter Param : Sig.Parameters)
            if (Param.Flags != 0 || Param.Name != null)
                break;
            else
                Num++;
        return Num;
    }
}

