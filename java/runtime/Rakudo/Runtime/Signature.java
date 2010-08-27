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
    public Signature(Parameter[] Parameters)
    {
        // Set parameters array in place.
        this.Parameters = Parameters;

        // Build and cache number of positionals.
        for (int i = 0; i < Parameters.length; i++)
            if (Parameters[i].Flags == Parameter.POS_FLAG)
            {
                NumRequiredPositionals++;
                NumPositionals++;
            }
            else if (Parameters[i].Flags == Parameter.OPTIONAL_FLAG)
                NumPositionals++;
            else
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
    int NumPositionals;
//  internal int NumPositionals;

    /// <summary>
    /// The number of required positionals.
    /// </summary>
    /// <returns></returns>
    int NumRequiredPositionals;
//  internal int NumRequiredPositionals;

    /// <summary>
    /// Do we have a slurpy positional parameter?
    /// </summary>
    /// <returns></returns>
    boolean HasSlurpyPositional()
//  internal boolean HasSlurpyPositional()
    {
        for (int i = 0; i < Parameters.length; i++)
            if (Parameters[i].Flags == Parameter.POS_SLURPY_FLAG)
                return true;
        return false;
    }
}

