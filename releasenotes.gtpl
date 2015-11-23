${title} - ${date}
${snippet}

| Download      | Description |
| ------------- |-------------|
<% assets.each{ asset -> %>| <%= "[" + asset.title + "](" + asset.download + ")" %> | <%= asset.description %> |\n<%}%>