package Rakudo.Runtime;

import Rakudo.Runtime.Parameter;




/// <summary>
/// Represents a signature.
/// </summary>
public class Signature
{
    /// <summary>
    /// Creates a new Signature object instance.
    /// </summary>
    /// <param name="Parameters"></param>
    public Signature(Parameter[] parameters)
    {
        // Set parameters array in place.
        this.Parameters = parameters;

        // Build and cache number of positionals.
        for (int i = 0; i < parameters.length; i++)
            if (parameters[i].Flags == Parameter.POS_FLAG)
            {
                NumRequiredPositionals++;
                NumPositionals++;
            }
            else if (Parameters[i].Flags == Parameter.OPTIONAL_FLAG)
                NumPositionals++;
            else // XXX rewrite as a switch?
                break;
    }

    /// <summary>
    /// The parameters we have.
    /// </summary>
    public Parameter[] Parameters;

    /// <summary>
    /// The total number of positionals.
    /// </summary>
    /// <returns></returns>
    public int NumPositionals; // C# has internal

    /// <summary>
    /// The number of required positionals.
    /// </summary>
    /// <returns></returns>
    public int NumRequiredPositionals; // C# has internal

    /// <summary>
    /// Do we have a slurpy positional parameter?
    /// </summary>
    /// <returns></returns>
    public boolean HasSlurpyPositional() // C# has internal
    {
        for (int i = 0; i < Parameters.length; i++)
            if (Parameters[i].Flags == Parameter.POS_SLURPY_FLAG)
                return true;
        return false;
    }
}

