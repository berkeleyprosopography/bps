{include file="header.tpl"}

<h1>Request a new password</h1>
{if $message}
<div class="formError">
	{$message}
</div>
{/if}
<form method="post" class="delphiForm">
	
	<label for="user">Username <span class="requiredFieldIndicator">*</span></label>
	<input type="text" name="user" maxlength="40" />
	<div class="buttonRow">
		<input type="submit" name="subreq" value="Submit"/>
	</div>
</form>

{include file="footer.tpl"}
