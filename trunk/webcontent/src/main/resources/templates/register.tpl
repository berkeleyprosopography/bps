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
{if $supportOpenReg || $currentUser_isAdmin || $currentUser_isAuthStaff }
<form action="/register" method="post" class="bpsForm">
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
{else}
<p>Thanks for you interest in Berkeley Prosopography Services!</p>  
<p>The system does not currently allow self-registration. 
If you would like to have an account, please use the
<a href="/contact">Contact us</a> link to request an account.</p>
{/if}

{include file="footer.tpl"}
