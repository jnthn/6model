package Rakudo.Runtime;

import Rakudo.Metamodel.RakudoObject;

/// <summary>
/// Represents a parameter in a signature.
/// </summary>
public class Parameter
{
    /// <summary>
    /// Creates a new parameter object instance.
    /// </summary>
    /// <param name="Type"></param>
    /// <param name="VariableName"></param>
    /// <param name="Name"></param>
    /// <param name="Flags"></param>
    public Parameter(RakudoObject Type, String VariableName, int VariableLexpadPosition, String Name, int Flags)
    {
        this.Type = Type;
        this.VariableName = VariableName;
        this.VariableLexpadPosition = VariableLexpadPosition;
        this.Name = Name;
        this.Flags = Flags;
    }

    /// <summary>
    /// The type of the parameter.
    /// </summary>
    public RakudoObject Type;

    /// <summary>
    /// The name of the lexical to bind the parameter to.
    /// </summary>
    public String VariableName;

    /// <summary>
    /// The position in the lexpad where the variable will be stored.
    /// </summary>
    public int VariableLexpadPosition;

    /// <summary>
    /// Name, for named parameters.
    /// </summary>
    public String Name;

    /// <summary>
    /// Parameter flags.
    /// </summary>
    public int Flags;

    /// <summary>
    /// (Un-)flag for positional parameters.
    /// </summary>
    public static final int POS_FLAG = 0;

    /// <summary>
    /// Flag for optional parameters.
    /// </summary>
    public static final int OPTIONAL_FLAG = 1;
    
    /// <summary>
    /// Flag for slurpy positional parameters.
    /// </summary>
    public static final int POS_SLURPY_FLAG = 2;

    /// <summary>
    /// Flag for named slurpy parameters.
    /// </summary>
    public static final int NAMED_SLURPY_FLAG = 4;

    /// <summary>
    /// Flag for named parameters.
    /// </summary>
    public static final int NAMED_FLAG = 8;
}

