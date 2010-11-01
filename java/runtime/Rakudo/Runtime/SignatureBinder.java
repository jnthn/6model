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
    public static void Bind(Context C, RakudoObject capture)
    {
        // Make sure the object is really a low level capture (don't handle
        // otherwise yet) and grab the pieces.
        P6capture.Instance nativeCapture = (P6capture.Instance)capture;
        if (nativeCapture == null)
            throw new UnsupportedOperationException("Can only deal with native captures at the moment");
        RakudoObject[] positionals = nativeCapture.Positionals != null ? nativeCapture.Positionals : EmptyPos;
        HashMap<String,RakudoObject> nameds = nativeCapture.Nameds != null ? nativeCapture.Nameds : EmptyNamed;

        // If we have no signature, that's same as an empty signature.
        Signature sig = C.StaticCodeObject.Sig;
        if (sig == null)
            return;

        // Current positional.
        int curPositional = 0;

        // Iterate over the parameters.
        Parameter[] params = sig.Parameters;
        int numParams = params.length;
        for (int i = 0; i < numParams; i++)
        {
            Parameter param = params[i];

            // Positional required?
            if (param.Flags == Parameter.POS_FLAG)
            {
                if (curPositional < positionals.length)
                {
                    // We have an argument, just bind it.
                    C.LexPad.Storage[param.VariableLexpadPosition] = positionals[curPositional];
                }
                else
                {
                    throw new UnsupportedOperationException("Not enough positional parameters; got " +
                        Integer.toString(curPositional) + " but needed " +
                        Integer.toString(NumRequiredPositionals(C.StaticCodeObject.Sig)));
                }

                // Increment positional counter.
                curPositional++;
            }

            // Positional optional?
            else if (param.Flags == Parameter.OPTIONAL_FLAG)
            {
                if (curPositional < positionals.length)
                {
                    // We have an argument, just bind it.
                    C.LexPad.Storage[param.VariableLexpadPosition] = positionals[curPositional];
                    curPositional++;
                }
                else
                {
                    // XXX Default value, vivification.
                }
            }

            // Named slurpy?
            else if ((param.Flags & Parameter.NAMED_SLURPY_FLAG) != 0)
            {
                throw new UnsupportedOperationException("Named slurpy parameters are not yet implemented.");
            }

            // Named positional?
            else if ((param.Flags & Parameter.POS_SLURPY_FLAG) != 0)
            {
                throw new UnsupportedOperationException("Positional slurpy parameters are not yet implemented.");
            }

            // Named?
            else if (param.Name != null)
            {
                // Yes, try and get argument.
                if (nameds.containsKey(param.Name))
                {
                    // We have an argument, just bind it.
                    RakudoObject value = nameds.get(param.Name);
                    C.LexPad.Storage[param.VariableLexpadPosition] = value;
                }
                else
                {
                    // Optional?
                    if ((param.Flags & Parameter.OPTIONAL_FLAG) == 0)
                    {
                        throw new UnsupportedOperationException("Required named parameter " + param.Name + " missing");
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
        int possiesInCapture = positionals.length;
        if (curPositional != possiesInCapture)
            throw new UnsupportedOperationException("Too many positional arguments passed; expected " +
                Integer.toString(NumRequiredPositionals(C.StaticCodeObject.Sig)) +
                " but got " + Integer.toString(possiesInCapture));

        // XXX TODO; Ensure we don't have leftover nameds.
    }

    /// <summary>
    /// The number of positionals we require.
    /// </summary>
    /// <param name="Sig"></param>
    /// <returns></returns>
    private static int NumRequiredPositionals(Signature sig)
    {
        int num = 0;
        for (Parameter param : sig.Parameters)
            if (param.Flags != 0 || param.Name != null)
                break;
            else
                num++;
        return num;
    }
}
