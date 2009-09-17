{include file="header.tpl"}

<h1>Register</h1>
{if count($messages) > 0}
<div class="formError">
	<ul>
	{section name=message loop=$messages}
		<li>{$messages[message]}</li>
	{/section}
	</ul>
</div>
{/if}
<form action="{$wwwroot}/modules/auth/register.php" method="post" class="bpsForm">
	<label for="user">Username <span class="requiredFieldIndicator">*</span></label>
	<input class="bpsFormInput" type="text" name="user" maxlength="40" value="{$user}">
	
	<label for="pass">Password <span class="requiredFieldIndicator">*</span></label>
	<input class="bpsFormInput" type="password" name="pass" maxlength="40" maxlength="25"/>
	
	<label for="pass2">Retype your password <span class="requiredFieldIndicator">*</span></label>
	<input class="bpsFormInput" type="password" name="pass2" maxlength="40"/>
	
	<label for="email">Email <span class="requiredFieldIndicator">*</span></label>
	<input class="bpsFormInput" type="text" name="email" maxlength="70" value="{$email}"/>
	<div class="buttonRow">
		<input type="submit" name="subjoin" value="Register"/>
	</div>
</form>

{include file="footer.tpl"}
