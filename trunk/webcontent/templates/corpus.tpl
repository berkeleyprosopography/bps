{include file="header.tpl"}

	<h1>Corpus Details</h1>
{if isset($errmsg) }
	<h2>{$errmsg}</h2>
{else}
	{if !isset($corpus) }
		<p>No corpus specified!</p>
	{else}
		<p>&nbsp;</p>
		<table border="0" cellspacing="0" cellpadding="5px">
			<tr>
				 <td class="title 2" width="200px">Corpus Name</td>
				 <td class="title" width="320px">Description</td>
				{if isset($canUpdateCorpus) }
				 <td class="title" width="100px">&nbsp;</td>
				{/if}
			</tr>
		</table>
		<div class="form_row">
			<form class="form_row" method="post">
				<input type="hidden" name="id" value="{$corpus.id}" />
				<table class="form_row" border="0" cellspacing="0" cellpadding="4px">
					<tr>
						<td class="corpus corpusname 2" width="200px">{$corpus.name}</td>
					{if !isset($canUpdateCorpus) }
						<td class="corpus corpusdesc 2" width="320px">
							{$corpus.description}
						</td>
					{else}
						<td class="corpus corpusdesc 2" width="320px">
							<textarea id="D_{$corpus.id}" cols="40" rows="2"
								onkeyup="enableElement('U_{$corpus.id}')"
								>{$corpus.description}</textarea>
						</td>
						<td class="corpus" width="100px">
							<input disabled id="U_{$corpus.id}" type="button"
								onclick="updateCorpus('{$corpus.id}')" value=" Update " />
						</td>
					{/if}
					</tr>
				</table>
			</form>
		</div>
		<p>&nbsp;</p>
		<h3>Upload a TEI Corpus file to verify Names and Dates:</h3>
		<div class="form_row">
			<form enctype="multipart/form-data" action="../../api/teiNamesAndDates.php" method="POST">
				<!-- MAX_FILE_SIZE must precede the file input field -->
				<input type="hidden" name="MAX_FILE_SIZE" value="{$maxfilesize}" />
				<!-- Name of input element determines name in $_FILES array -->
				<table class="form_row" border="0" cellspacing="0" cellpadding="4px">
					<tr height="40px">
						<td class="corpus corpusname 2" width="200px">Select TEI file to upload:</td>
						<td colspan="2" class="corpus corpusdesc 2" >
							<input name="teifile" type="file" size="44" style="padding:10px" />
							&nbsp;<input type="submit" value="Upload File" />
						</td>
					</tr>
				</table>
			</form>
		</div>
	{/if}
	{if isset($opmsg) }
		<p>&nbsp;</p>
		<p>{$opmsg}</p>
	{/if}
{/if}
{include file="footer.tpl"}
