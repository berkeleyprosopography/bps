{include file="header.tpl"}
<div id="contentNarrow">
<h2>Profile information for {$currentUser_name} </h2>

{if count($messages) > 0}
<div class="formError">
	<ul>
	{section name=message loop=$messages}
		<li>{$messages[message]}</li>
	{/section}
	</ul>
</div>
{/if}
<form action="" method="post" class="bpsForm">
	<fieldset>
		<legend>Account settings</legend>
	<label for="email">Email address <span class="requiredFieldIndicator">*</span></label>
	<input class="bpsFormInput" type="text" name="email" maxlength="70" size="50" value="{$email}"/>
	
	<label for="pass">Change your password</label>
	<input class="bpsFormInput" type="password" name="pass" maxlength="40"/>
	
	<label for="pass2">Repeat your new password</label>
	<input class="bpsFormInput" type="password" name="pass2" maxlength="40"/>
	</fieldset>
	<fieldset>
		<legend>Profile information</legend>
	<label for="real_name">Real name</label>
	<input class="bpsFormInput" type="text" name="real_name" maxlength="70" value="{$real_name}"/>
	
	<label for="website_url">Website URL</label>
	<input class="bpsFormInput" type="text" name="website_url" maxlength="255" size="50" value="{$website_url}"/>
	
	<label for="about">About you</label>
	<textarea class="bpsFormInput" name="about" rows="15" cols="30">{$about}</textarea>
	</fieldset>
	<div class="buttonRow">
		<input type="submit" name="subreq" value="Update"/> or <a href="/profile/?uid={$currentUser_id}"> Discard changes</a>
	</div>
</form>
</div>
{include file="footer.tpl"}
