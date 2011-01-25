package Rakudo.Runtime;

import Rakudo.Metamodel.RakudoObject;
import Rakudo.Runtime.DefinednessConstraint;




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
    public Parameter(RakudoObject type, String variableName, int variableLexpadPosition,
        String name, int flags, DefinednessConstraint definedness, RakudoObject defaultValue)
    {
        this.Type = type;
        this.VariableName = variableName;
        this.VariableLexpadPosition = variableLexpadPosition;
        this.Name = name;
        this.DefaultValue = defaultValue;
        this.Flags = flags;
        this.Definedness = definedness;
    }

    /// <summary>
    /// The type of the parameter.
    /// </summary>
    public RakudoObject Type;

    /// <summary>
    /// Whether a defined or undefined value is required.
    /// </summary>
    public DefinednessConstraint Definedness;

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
    /// Default RakudoObject for optional parameters.
    /// </summary>
    public RakudoObject DefaultValue;

    /// <summary>
    /// Parameter flags.
    /// </summary>
    public int Flags;

    /// <summary>
    /// (Un-)flag for positional parameters.
    /// </summary>
    public static final int POS_FLAG = 0; // C# has public const int

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

    /// <summary>
    /// Tests whether the flag is optional.
    /// </summary>
    /// <returns></returns>
    public boolean IsOptional()
    {
        return (Flags & OPTIONAL_FLAG) > 0;
    }
}

