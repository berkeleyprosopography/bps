{include file="header.tpl"}
 <div class="admin_hdr" >
	<p id="orient"><a href="admin.php">Admin Main</a> -> Edit Permission Definitions</p>
 </div>

{if isset($perm_error) }
	<h2>{$perm_error}</h2>
{else}
	{if !isset($perms) }
		<p>There are no permissions defined!</p>
	{else}
			<table border="0" cellspacing="0" cellpadding="5px">
				<tr>
					 <td class="title" width="100px">&nbsp;</td>
					 <td class="title 2" width="200px">Permission Name</td>
					 <td class="title" width="320px">Description</td>
					 <td class="title" width="100px">&nbsp;</td>
				</tr>
			</table>
		{section name=perm loop=$perms}
			<form class="form_row" method="post">
				<input type="hidden" name="perm" value="{$perms[perm].name}" />
				<table class="form_row" border="0" cellspacing="0" cellpadding="4px">
					<tr>
						<td class="perm" width="100px"><input type="submit" name="delete" value="Delete" /></td>
						<td class="perm permname 2" width="200px">{$perms[perm].name}</td>
						<td class="perm permdesc">
							<textarea id="D_{$perms[perm].name}" cols="40" rows="2"
								onkeyup="enableElement('U_{$perms[perm].name}')"
								>{$perms[perm].description}</textarea>
						</td>
						<td class="perm" width="100px">
							<input disabled id="U_{$perms[perm].name}" type="button"
								onclick="updatePerm('{$perms[perm].name}')" value="Update" />
						</td>
					</tr>
				</table>
			</form>
		{/section}
	{/if}
	<div style="height:10px"></div>
	<form method="post" onsubmit="checkValues(event, this.perm, this.desc, 255);">
		<table border="0" cellspacing="0" cellpadding="5">
			<tr>
				<td class="title" colspan="4">Add a New Permission</td>
			</tr>
			<tr>
				<td class="perm_label" width="100px">Name:</td>
				<td class="2" width="200px"><input type="text" name="perm" maxlength="40"></td>
				<td class="permdesc"><textarea name="desc" rows="2" cols="40" 
				       onkeypress="limitChars(this,255);"
							 onblur="limitChars(this,255);"
							 onfocus="limitChars(this,255);"></textarea></td>
				<td><input type="submit" name="add" value="Add" /></td>
			</tr>
		</table>
	</form>
	{if isset($opmsg) }
		<p>{$opmsg}</p>
	{/if}
{/if}
{include file="footer.tpl"}
