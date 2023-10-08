package at.uibk.dps.rm.util.misc;

import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements tests for the {@link ConsoleOutputUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ConsoleOutputUtilityTest {

    private final String output1 = "\u001B[0m\u001B[1mInitializing the backend...\u001B[0m\n" +
        "\u001B[0m\u001B[1mInitializing modules...\u001B[0m\n" +
        "- aws_us_east_1 in aws_us_east_1\n" +
        "- aws_us_east_1.lambda in ..\\..\\terraform\\aws\\faas\n" +
        "\n" +
        "\u001B[0m\u001B[1mInitializing provider plugins...\u001B[0m\n" +
        "- Finding hashicorp/kubernetes versions matching \"2.20.0\"...\n" +
        "- Finding hashicorp/aws versions matching \">= 4.16.0, ~> 4.16\"...\n" +
        "- Installing hashicorp/kubernetes v2.20.0...\n" +
        "- Installed hashicorp/kubernetes v2.20.0 (signed by HashiCorp)\n" +
        "- Installing hashicorp/aws v4.67.0...\n" +
        "- Installed hashicorp/aws v4.67.0 (signed by HashiCorp)\n" +
        "\n" +
        "Terraform has created a lock file \u001B[1m.terraform.lock.hcl\u001B[0m to record the provider\n" +
        "selections it made above. Include this file in your version control repository\n" +
        "so that Terraform can guarantee to make the same selections by default when\n" +
        "you run \"terraform init\" in the future.\u001B[0m\n" +
        "\n" +
        "\u001B[0m\u001B[1m\u001B[32mTerraform has been successfully initialized!\u001B[0m\u001B[32m\u001B[0m\n" +
        "\u001B[0m\u001B[32m\n" +
        "You may now begin working with Terraform. Try running \"terraform plan\" to see\n" +
        "any changes that are required for your infrastructure. All Terraform commands\n" +
        "should now work.\n" +
        "\n" +
        "If you ever set or change modules or backend configuration for Terraform,\n" +
        "rerun this command to reinitialize your working directory. If you forget, other\n" +
        "commands will detect it and remind you to do so if necessary.\u001B[0m)";

    private final String result1 = "Initializing the backend...\n" +
        "Initializing modules...\n" +
        "- aws_us_east_1 in aws_us_east_1\n" +
        "- aws_us_east_1.lambda in ..\\..\\terraform\\aws\\faas\n" +
        "\n" +
        "Initializing provider plugins...\n" +
        "- Finding hashicorp/kubernetes versions matching \"2.20.0\"...\n" +
        "- Finding hashicorp/aws versions matching \">= 4.16.0, ~> 4.16\"...\n" +
        "- Installing hashicorp/kubernetes v2.20.0...\n" +
        "- Installed hashicorp/kubernetes v2.20.0 (signed by HashiCorp)\n" +
        "- Installing hashicorp/aws v4.67.0...\n" +
        "- Installed hashicorp/aws v4.67.0 (signed by HashiCorp)\n" +
        "\n" +
        "Terraform has created a lock file .terraform.lock.hcl to record the provider\n" +
        "selections it made above. Include this file in your version control repository\n" +
        "so that Terraform can guarantee to make the same selections by default when\n" +
        "you run \"terraform init\" in the future.\n" +
        "\n" +
        "Terraform has been successfully initialized!\n" +
        "\n" +
        "You may now begin working with Terraform. Try running \"terraform plan\" to see\n" +
        "any changes that are required for your infrastructure. All Terraform commands\n" +
        "should now work.\n" +
        "\n" +
        "If you ever set or change modules or backend configuration for Terraform,\n" +
        "rerun this command to reinitialize your working directory. If you forget, other\n" +
        "commands will detect it and remind you to do so if necessary.)";

    private final String output2 = "\u001B[31mâ•·\u001B[0m\u001B[0m\n" +
        "\u001B[31mâ”‚\u001B[0m \u001B[0m\u001B[1m\u001B[31mError: \u001B[0m\u001B[0m\u001B[1mconfiguring Terraform " +
        "AWS Provider: validating provider credentials: retrieving caller identity from STS: operation error STS: " +
        "GetCallerIdentity, https response error StatusCode: 403, RequestID: 05374e28-7699-4a64-a8d5-2e8f35fb1d35, " +
        "api error InvalidClientTokenId: The security token included in the request is invalid.\u001B[0m\n" +
        "\u001B[31mâ”‚\u001B[0m \u001B[0m\n" +
        "\u001B[31mâ”‚\u001B[0m \u001B[0m\u001B[0m  with module.aws_us_east_1.provider[\"registry.terraform.io" +
        "/hashicorp/aws\"],\n" +
        "\u001B[31mâ”‚\u001B[0m \u001B[0m  on aws_us_east_1\\main.tf line 1, in provider \"aws\":\n" +
        "\u001B[31mâ”‚\u001B[0m \u001B[0m   1: provider \"aws\" \u001B[4m{\u001B[0m\u001B[0m\n" +
        "\u001B[31mâ”‚\u001B[0m \u001B[0m\n" +
        "\u001B[31mâ•µ\u001B[0m\u001B[0m";

    private final String result2 = "â•·\n" +
        "â”‚ Error: configuring Terraform AWS Provider: validating provider credentials: retrieving caller identity" +
        " from STS: operation error STS: GetCallerIdentity, https response error StatusCode: 403, RequestID: " +
        "05374e28-7699-4a64-a8d5-2e8f35fb1d35, api error InvalidClientTokenId: The security token included in the " +
        "request is invalid.\n" +
        "â”‚ \n" +
        "â”‚   with module.aws_us_east_1.provider[\"registry.terraform.io/hashicorp/aws\"],\n" +
        "â”‚   on aws_us_east_1\\main.tf line 1, in provider \"aws\":\n" +
        "â”‚    1: provider \"aws\" {\n" +
        "â”‚ \n" +
        "â•µ";

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void escapeConsoleOutput(int outputIndex) {
        String output = outputIndex == 1 ? output1 : output2;
        String expected = outputIndex == 1 ? result1 : result2;

        String result = ConsoleOutputUtility.escapeConsoleOutput(output);

        assertThat(result).isEqualTo(expected);
    }
}
