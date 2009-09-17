{include file="header.tpl"}

{if $id<0}
<h1>Add News Item</h1>
<p>Use this form to set information for a new news item.</p>
{else}
<h1>Edit News Item</h1>
<p>Use this form to update information for a news item.</p>
{/if}
{if count($messages) > 0}
<div class="formError">
	<ul>
	{section name=message loop=$messages}
		<li>{$messages[message]}</li>
	{/section}
	</ul>
</div>
{/if}
<form action="" method="post" accept-charset="utf-8" class="delphiForm">
	<label for="header">Headline</label>	
	<input class="delphiFormInput" type="text" name="header" value="{$header}" id="header"  style="width:400px"/>

	<label for="content">News content</label>
	<textarea class="delphiFormInput" name="content" rows="8" cols="40" style="width:400px">{$content}</textarea>
	
	<div class="buttonRow">
		<input type="submit" name="submit" value={if $id<0}"Add Item"{else}"Update Item"{/if}/>
		<input type="button" value="Cancel" onClick="window.location.href='admin.php'"/>
	</div>
	<input type="hidden" name="id" value="{$id}" id="id" />
</form>


{include file="footer.tpl"}
