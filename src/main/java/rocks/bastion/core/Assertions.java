package rocks.bastion.core;

/**
 * Specifies assertions to apply to a response model received after performing a Bastion request. The assertions
 * are checked only if Bastion can bind a model object to the response's content successfully.
 *
 * Technically, the assertions can be any executable code but it is strongly recommended that a user only performs
 * stateless checks on the provided model and response. If you would like to change the state of the current test
 * you can use the {@link Bastion#thenDo(Callback)} method using a {@link Callback}. You can also retrieve the returned
 * object using the {@link Bastion#getModel()} and {@link Bastion#getResponse()} methods.
 *
 * @param <M> The type of response model the assertions will apply for.
 */
@FunctionalInterface
public interface Assertions<M> {

    /**
     * The constant representing the assertions which always pass.
     */
    Assertions<?> NO_ASSERTIONS = ((statusCode, response, model) -> {
    });

    /**
     * Returns a typed no-op assertion which always passes.
     *
     * @param <M> The type of response model the assertions will apply for.
     * @return A no-op assertions object which does nothing and always passes.
     */
    @SuppressWarnings("unchecked")
    static <M> Assertions<M> noAssertions() {
        return (Assertions<M>) NO_ASSERTIONS;
    }

    /**
     * Asserts that the given response, retrieved after Bastion performs an HTTP request, is correct. To indicate
     * that the response did not pass assertions, this method throws {@link AssertionError}. Note that this is important
     * because Bastion makes a distinction between a test containing an error (ie. any exception which is not {@linkplain AssertionError})
     * and one which has a failed assertion. This difference will be made visible in a testing framework which is running a Bastion test.
     *
     * @param statusCode The HTTP status code received for the response.
     * @param response   A representation of the HTTP response which was received.
     * @param model      The model object that was created by binding the HTTP response body to an actual Java object.
     * @throws AssertionError Thrown if the HTTP response does not pass the assertions.
     */
    void execute(int statusCode, ModelResponse<? extends M> response, M model) throws AssertionError;

}
