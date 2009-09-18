{include file="header.tpl"}
<div id="contentNarrow">
	

<h1>Login</h1>
{if $message}
<div class="formError">
	{$message}
</div>
{/if}
<form action="{$wwwroot}/login/" method="post" class="bpsForm">
	<div>
		<label for="user">Username <span class="requiredFieldIndicator">*</span></label>
		<input type="text" name="user" maxlength="40" id="user" class="bpsFormInput"/>

		<label for="pass">Password <span class="requiredFieldIndicator">*</span></label>
		<input type="password" name="pass" maxlength="40" id="pass" class="bpsFormInput"/>
		<br/><br/>
		<input type="checkbox" name="remember"/> Remember me on this computer
		<div class="buttonRow">
			<input type="submit" name="sublogin" value="Login"/>
		</div>
		{if $redir}
			<input type="hidden" name="redir" value="{$redir}"/>
		{/if}
	</div>
</form>
<p><!-- <a href="register.php">Register</a> --><br/>
<a href="/lostpass/">Forgot your password?</a></p>
</div>
{include file="footer.tpl"}
