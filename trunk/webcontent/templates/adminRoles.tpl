{include file="header.tpl"}

 <div class="admin_hdr" >
	<p id="orient"><a href="admin.php">Admin Main</a> -> Edit Role Definitions</p>
 </div>

{if isset($perm_error) }
	<h2>{$perm_error}</h2>
{else}
	{if !isset($roles) }
		<p>There are no roles defined!</p>
	{else}
			<table border="0" cellspacing="0" cellpadding="5">
				<tr>
					 <td class="title" width="100px">&nbsp;</td>
					 <td class="title 2" width="200px">Role Name</td>
					 <td class="title" width="320px">Description</td>
					 <td class="title" width="100px">&nbsp;</td>
				</tr>
			</table>
		{section name=role loop=$roles}
			<form class="form_row" method="post">
				<input type="hidden" name="role" value="{$roles[role].name}" />
				<table border="0" cellspacing="0" cellpadding="5">
					<tr>
						<td class="role" width="100px"><input type="submit" name="delete" value="Delete" /></td>
						<td class="role rolename 2" width="200px"><p>{$roles[role].name}</p></td>
						<td class="role roledesc">
							<textarea id="D_{$roles[role].name}" cols="40" rows="2"
								onkeyup="enableElement('U_{$roles[role].name}')"
								>{$roles[role].description}</textarea>
						</td>
						<td class="role" width="100px">
							<input disabled id="U_{$roles[role].name}" type="button"
								onclick="updateRole('{$roles[role].name}')" value="Update" />
						</td>
					</tr>
				</table>
			</form>
		{/section}
	{/if}
	<div style="height:10px"></div>
	<form method="post" onsubmit="checkValues(event, this.role, this.desc, 255);" >
		<table border="0" cellspacing="0" cellpadding="5">
			<tr>
				<td class="title" colspan="4">Add a New Role</td>
			</tr>
			<tr>
				<td class="label" width="100px">Name:</td>
				<td class="2" width="200px"><input type="text" name="role" maxlength="40"></td>
				<td><textarea name="desc" rows="2" cols="40" 
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

