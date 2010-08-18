package Rakudo.Metamodel;
/// <summary>
/// This interface is implemented by types that we can box to
/// and unbox from.
/// </summary>
/// <typeparam name="TValue"></typeparam>
interface IBoxableRepresentation<TValue>
{
    /// <summary>
    /// For reprs that store a single native value, gets this.
    /// </summary>
    /// <param name="Object"></param>
    /// <returns></returns>
    TValue get_value(IRakudoObject Object);

    /// <summary>
    /// For reprs that store a single native value, sets this.
    /// </summary>
    /// <param name="Object"></param>
    /// <param name="?"></param>
    void set_value(IRakudoObject Object, TValue Value);
}

